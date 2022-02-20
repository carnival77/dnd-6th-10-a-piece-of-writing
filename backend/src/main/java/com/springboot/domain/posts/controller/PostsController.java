package com.springboot.domain.posts.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.springboot.domain.common.model.ResponseDto;
import com.springboot.domain.common.model.SuccessCode;
import com.springboot.domain.common.service.ResponseServiceImpl;
import com.springboot.domain.posts.model.dto.ExtractWordDto;
import com.springboot.domain.posts.model.dto.PostsDto;
//import com.springboot.domain.posts.model.dto.PostsListResponseDto;
//import com.springboot.domain.posts.model.dto.PostsResponseDto;
//import com.springboot.domain.posts.model.dto.PostsSaveRequestDto;
import com.springboot.domain.posts.service.PostsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostsController {

    private final PostsService postsService;

    private final ResponseServiceImpl responseServiceImpl;

    // 0219 변경 예정. author -> member
    // 업로드
    @Operation(summary = "save posts api", description = "글귀 업로드 api")
    @PostMapping
    public ResponseEntity<ResponseDto> save(@RequestBody PostsDto requestDto) {

        Long savedPostId = postsService.save(requestDto);

        return responseServiceImpl.successResult(SuccessCode.SAVE_POSTS_SUCCESS, savedPostId);
    }

    // 수정
//    @PutMapping("/posts/{id}")
//    public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
//        return postsService.update(id, requestDto);
//    }

    // 삭제
    @Operation(summary = "delete posts api", description = "글귀 삭제 api")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable Long id) {

//        Long DeletedPostId = postsService.delete(id);
        Long DeletedPostId = postsService.removeWithReplies(id);

        return responseServiceImpl.successResult(SuccessCode.DELETE_POSTS_SUCCESS, DeletedPostId);
    }

    // 0219 변경 예정. author -> member
//    // 전체 게시물 내림차순 조회
//    @Operation(summary = "select all posts api", description = "모든 글귀 검색 api. request 받은 페이지 기준으로 메인 화면에서 글귀를 최신 순으로 페이지당 size개씩 조회.")
//    @GetMapping("/page/{page}")
//    public ResponseEntity<ResponseDto> findAllPostsOrderByIdDesc(@PathVariable int page) {
////        return postsService.findAllPostsOrderById();
//
//        List<PostsListResponseDto> posts = postsService.findAllPostsOrderByIdDesc(page);
//
//        return responseServiceImpl.successResult(SuccessCode.SELECT_ALL_POSTS_SUCCESS, posts);
//    }
//
//    // 검색 내용 포함 게시물 내림차순 조회
//    @Operation(summary = "select posts containing searched content api", description = "검색된 type(c=content, a=author, t=topic)과 keyword를 포함한 게시물 조회 api. 검색 화면에서 글귀를 최신 순으로 페이지당 size개씩 조회.")
//    @GetMapping("/type/{type}/keyword/{keyword}/page/{page}")
//    public ResponseEntity<ResponseDto> findAllPostsBySearch(@PathVariable int page,
//        @PathVariable String keyword, @PathVariable String type) {
//
//        List<PostsListResponseDto> posts = postsService.findAllPostsBySearch(page, keyword, type);
//
//        return responseServiceImpl.successResult(
//            SuccessCode.SELECT_POSTS_SEARCH_SUCCESS, posts);
//    }

    @ApiOperation(value = "이미지 텍스트 추출", notes = "이미지를 전송해 텍스트를 추출한다.")
    @PostMapping(value = "/img-extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> imageExtract(ExtractWordDto extractWordDto) {
        MultipartFile file = extractWordDto.getFile();

        GoogleCredentials credentials = postsService.getCredentials();
        String imageUrl = postsService.postsImgUpload(credentials, file,
            postsService.getFileUuid());
        String result = postsService.postsImgExtractWords(credentials, file, imageUrl);

        return responseServiceImpl.successResult(SuccessCode.EXTRACT_SUCCESS, result);
    }
}
