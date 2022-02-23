package com.springboot.domain.posts.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.ImageSource;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.springboot.domain.auth.model.UserDetailsImpl;
import com.springboot.domain.common.error.exception.BusinessException;
import com.springboot.domain.common.error.exception.EntityNotFoundException;
import com.springboot.domain.common.error.exception.ErrorCode;
import com.springboot.domain.common.model.ResponseDto;
import com.springboot.domain.common.model.SuccessCode;
import com.springboot.domain.common.service.ResponseService;
import com.springboot.domain.likes.model.Likes;
import com.springboot.domain.likes.repository.LikesRepository;
import com.springboot.domain.member.model.Member;
import com.springboot.domain.member.repository.MemberRepository;
import com.springboot.domain.posts.model.dto.MultipartDto;
import com.springboot.domain.member.model.Member;
import com.springboot.domain.posts.model.dto.PageRequestDto;
import com.springboot.domain.posts.model.dto.PageResultDto;
import com.springboot.domain.posts.model.dto.PostsDto;
import com.springboot.domain.posts.model.entity.Posts;
import com.springboot.domain.posts.repository.PostsRepository;
import com.springboot.domain.reply.repository.ReplyRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@Transactional
@Service
public class PostsServiceImpl implements PostsService {

    private final PostsRepository postsRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final ResponseService responseService;
    private final LikesRepository likesRepository;

    @Override
    public Posts findPostsById(Long id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Override
    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
    }

    @Override
    public Long save(PostsDto requestDto, MultipartDto multipartDto) {
        log.info(requestDto);

        String imageUrl = postsImgUpload(multipartDto.getFile(), getFileUuid());
        Posts posts = dtoToEntity(requestDto, imageUrl);
        postsRepository.save(posts);
        return posts.getId();
    }

    @Override
    public Long removePosts(Long postsId, UserDetailsImpl userDetails) {
        //댓글 부터 삭제
        replyRepository.deleteReplyByPostsId(postsId);
        likesRepository.deleteAllByPostsId(postsId);
        postsRepository.deletePostsById(postsId);
        return postsId;
    }

    @Override
    public List<PostsDto> findAllPostsOrderByIdDesc(int page, int size,
            UserDetailsImpl userDetails) {

        PageRequestDto pageRequestDTO = PageRequestDto.builder()
                .page(page)
                .size(size)
                .build();

        PageResultDto<PostsDto, Object[]> resultDTO = getList(pageRequestDTO, userDetails);
        return resultDTO.getDtoList();
    }

    @Override
    public List<PostsDto> findAllPostsBySearch(int page, int size, String keyword, String type,
            UserDetailsImpl userDetails) {

        PageRequestDto pageRequestDTO = PageRequestDto.builder()
                .page(page)
                .size(size)
                .type(type)
                .keyword(keyword)
                .build();

        PageResultDto<PostsDto, Object[]> resultDTO = getList(pageRequestDTO, userDetails);
        return resultDTO.getDtoList();
    }

    @Override
    public String getFileUuid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public GoogleCredentials getCredentials() {
        try {
            ClassPathResource resource = new ClassPathResource("/gcloud-auth.json");
            return GoogleCredentials.fromStream(resource.getInputStream());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CREDENTIAL_ERROR);
        }
    }

    @Override
    public String postsImgUpload(MultipartFile multipartFile,
            String fileName) {
        try {
            byte[] bytes = multipartFile.getBytes();

            String projectId = "decent-destiny-321408";
            String bucketName = "example-ocr-test";

            Storage storage = StorageOptions.newBuilder().setProjectId(projectId)
                    .setCredentials(getCredentials()).build().getService();

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();

            storage.create(blobInfo, bytes);
            storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Role.READER));

            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_INPUT_INVALID);
        }
    }

    @Override
    public String postsImgExtractWords(MultipartFile multipartFile, String imageUrl) {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try {
            ImageAnnotatorSettings imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                    .build();

            ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings);
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            AnnotateImageResponse res = responses.get(0);
            if (res.hasError()) {
                throw new BusinessException(ErrorCode.IMAGE_URL_INPUT_INVALID);
            }
            return res.getTextAnnotationsList().get(0).getDescription();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.VISION_API_ERROR);
        }
    }

    // Tools for Pagination
    @Override
    public PageResultDto<PostsDto, Object[]> getList(PageRequestDto pageRequestDTO,
            UserDetailsImpl userDetails) {

        log.info(pageRequestDTO);

        Function<Object[], PostsDto> fn = (en -> entityToDTO((Posts) en[0], (Member) en[1],
                userDetails.getMember().getId()));

        Page<Object[]> result = postsRepository.searchPage(
                pageRequestDTO.getType(),
                pageRequestDTO.getKeyword(),
                pageRequestDTO.getPageable(Sort.by("id").descending()));

        return new PageResultDto<>(result, fn);
    }

    @Override
    public ResponseEntity<ResponseDto> likePost(UserDetailsImpl userDetailsImpl, Long id) {
        Member member = userDetailsImpl.getMember();
        Posts posts = findPostsById(id);
        likesRepository.save(Likes.builder().member(member).posts(posts).build());
        return responseService.successResult(SuccessCode.LIKE_SUCCESS);
    }

    @Override
    public ResponseEntity<ResponseDto> disLikePost(UserDetailsImpl userDetailsImpl, Long id) {
        Member member = userDetailsImpl.getMember();
        Posts posts = findPostsById(id);
        likesRepository.deleteLikesByMemberAndPosts(member, posts);
        return responseService.successResult(SuccessCode.DISLIKE_SUCCESS);
    }

    @Override
    public PostsDto get(Long id, UserDetailsImpl userDetails) {
        Object result = postsRepository.getPostsWithAuthor(id);
        Object[] arr = (Object[]) result;
        return entityToDTO((Posts) arr[0], (Member) arr[1], userDetails.getMember().getId());
    }
}
