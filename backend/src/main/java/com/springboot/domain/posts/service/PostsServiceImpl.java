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
import com.springboot.domain.common.error.exception.BusinessException;
import com.springboot.domain.common.error.exception.ErrorCode;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RequiredArgsConstructor
@Service
public class PostsServiceImpl implements PostsService {

    private final PostsRepository postsRepository;

    private final ReplyRepository replyRepository;

    // 0219 변경 예정. author -> member
//    @Override
//    @Transactional
//    public Long save(PostsSaveRequestDto requestDto) {
//
//        return postsRepository.save(requestDto.toEntity()).getId();
//    }
    @Override
    @Transactional
    public Long save(PostsDto requestDto) {

//        return postsRepository.save(requestDto.toEntity()).getId();

        log.info(requestDto);

        Posts posts = dtoToEntity(requestDto);

        postsRepository.save(posts);

        return posts.getId();
    }

//    @Transactional
//    public Long update(Long id, PostsUpdateRequestDto requestDto){
//        Posts posts = postsRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
//
//        posts.update(requestDto.getRef(),requestDto.getContent());
//
//        return id;
//    }

    //    @Override
//    @Transactional
//    public Long delete(Long id) {
//        Posts posts = postsRepository.findById(id)
//            .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다. id=" + id));
//
//        postsRepository.delete(posts);
//
//        return id;
//    }
    @Transactional
    @Override
    public Long removeWithReplies(Long postsId) {

        //댓글 부터 삭제
        replyRepository.deleteByPostsId(postsId);

        postsRepository.deleteById(postsId);

        return postsId;
    }

    // 0219 변경 예정. author -> member
//    @Override
//    public List<PostsListResponseDto> findAllPostsOrderByIdDesc(int page) {
//
//        // size = 10 임의 설정
//        int size = 10;
//
//        PageRequestDto pageRequestDTO = PageRequestDto.builder()
//            .page(page)
//            .size(size)
//            .build();
//
//        PageResultDto<PostsListResponseDto, Posts> resultDTO = getList(pageRequestDTO);
//
//        return resultDTO.getDtoList();
//    }

    @Override
    public List<PostsDto> findAllPostsOrderByIdDesc(int page, int size) {

        PageRequestDto pageRequestDTO = PageRequestDto.builder()
            .page(page)
            .size(size)
            .build();

        PageResultDto<PostsDto, Object[]> resultDTO = getList(pageRequestDTO);

        return resultDTO.getDtoList();
    }

    // 0219 변경 예정. author -> member
//    @Override
//    public List<PostsListResponseDto> findAllPostsBySearch(int page, String keyword, String type) {
//
//        // size = 10 임의 설정
//        int size = 10;
//
//        PageRequestDto pageRequestDTO = PageRequestDto.builder()
//            .page(page)
//            .size(size)
//            .type(type)
//            .keyword(keyword)
//            .build();
//
//        PageResultDto<PostsListResponseDto, Posts> resultDTO = getList(pageRequestDTO);
//
//        return resultDTO.getDtoList();
//    }
    @Override
    public List<PostsDto> findAllPostsBySearch(int page, int size, String keyword, String type) {

        PageRequestDto pageRequestDTO = PageRequestDto.builder()
            .page(page)
            .size(size)
            .type(type)
            .keyword(keyword)
            .build();

        PageResultDto<PostsDto, Object[]> resultDTO = getList(pageRequestDTO);

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
    public String postsImgUpload(GoogleCredentials credentials, MultipartFile multipartFile,
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
    public String postsImgExtractWords(GoogleCredentials credentials, MultipartFile multipartFile,
        String imageUrl) {
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
    // 0219 변경 예정. author -> member
//    @Override
//    public PageResultDto<PostsListResponseDto, Posts> getList(PageRequestDto requestDTO) {
//
//        Pageable pageable = requestDTO.getPageable(Sort.by("id").descending());
//
//        BooleanBuilder booleanBuilder = getSearch(requestDTO); //검색 조건 처리
//
//        Page<Posts> result = postsRepository.findAll(booleanBuilder, pageable); //Querydsl 사용
//
//        Function<Posts, PostsListResponseDto> fn = (entity -> entityToDto(entity));
//
////        Function<Posts, PostsListResponseDto> fn = (PostsListResponseDto::new);
//
//        return new PageResultDto<>(result, fn);
//    }

    @Override
    public PageResultDto<PostsDto, Object[]> getList(PageRequestDto pageRequestDTO) {

        log.info(pageRequestDTO);

//        Function<Object[], PostsDto> fn = (en -> entityToDTO((Posts)en[0],(Member)en[1],(Reply)en[2]));

        Function<Object[], PostsDto> fn = (en -> entityToDTO((Posts) en[0], (Member) en[1]));

//        Page<Object[]> result = postsRepository.getPostsListWithAuthor(
//                pageRequestDTO.getPageable(Sort.by("id").descending())  );
        Page<Object[]> result = postsRepository.searchPage(
            pageRequestDTO.getType(),
            pageRequestDTO.getKeyword(),
            pageRequestDTO.getPageable(Sort.by("id").descending()));

        return new PageResultDto<>(result, fn);
    }

    @Override
    public PostsDto get(Long id) {

        Object result = postsRepository.getPostsWithAuthor(id);

        Object[] arr = (Object[]) result;

        return entityToDTO((Posts) arr[0], (Member) arr[1]);
    }

    // 0219 변경 예정. author -> member
//    private BooleanBuilder getSearch(PageRequestDto requestDTO) {
//
//        String type = requestDTO.getType();
//
//        BooleanBuilder booleanBuilder = new BooleanBuilder();
//
//        QPosts qPosts = QPosts.posts;
//
//        String keyword = requestDTO.getKeyword();
//
//        BooleanExpression expression = qPosts.id.gt(0L); // id > 0 조건만 생성
//
//        booleanBuilder.and(expression);
//
//        if (type == null || type.trim().length() == 0) { //검색 조건이 없는 경우
//            return booleanBuilder;
//        }
//
//        //검색 조건을 작성하기
//        BooleanBuilder conditionBuilder = new BooleanBuilder();
//
//        // topic 적용 예정
////        if(type.contains("t")){
////            conditionBuilder.or(qPosts.topic.contains(keyword));
////        }
//        if (type.contains("c")) {
//            conditionBuilder.or(qPosts.content.contains(keyword));
//        }
//        if (type.contains("a")) {
//            conditionBuilder.or(qPosts.author.contains(keyword));
//        }
//
//        //모든 조건 통합
//        booleanBuilder.and(conditionBuilder);
//
//        return booleanBuilder;
//    }
}
