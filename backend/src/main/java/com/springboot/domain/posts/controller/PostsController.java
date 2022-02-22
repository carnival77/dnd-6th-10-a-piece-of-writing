package com.springboot.domain.posts.controller;

import com.springboot.domain.auth.model.UserDetailsImpl;
import com.springboot.domain.common.model.ResponseDto;
import com.springboot.domain.common.model.SuccessCode;
import com.springboot.domain.common.service.ResponseServiceImpl;
import com.springboot.domain.posts.model.dto.ExtractWordDto;
import com.springboot.domain.posts.model.dto.PostsDto;
import com.springboot.domain.posts.model.dto.MultipartDto;
import com.springboot.domain.posts.model.dto.PostsListResponseDto;
import com.springboot.domain.posts.model.dto.PostsSaveRequestDto;
import com.springboot.domain.posts.service.PostsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostsController {
    private final PostsService postsService;

    private final ResponseServiceImpl responseServiceImpl;

    // 업로드
    @Operation(summary = "save posts api", description = "글귀 업로드 api")
    @PostMapping
//    public ResponseEntity<ResponseDto> save(
//            @ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
//            @RequestPart("ref") String ref, @RequestPart("content") String content,
//            @Valid MultipartDto multipartDto) {
//        return responseServiceImpl.successResult(SuccessCode.SAVE_POSTS_SUCCESS,
//                postsService.save(ref, content, multipartDto, userDetailsImpl));
    public ResponseEntity<ResponseDto> save(@RequestBody PostsDto requestDto) {

        Long savedPostId = postsService.save(requestDto);

        return responseServiceImpl.successResult(SuccessCode.SAVE_POSTS_SUCCESS, savedPostId);
    }

    // 삭제
    @Operation(summary = "delete posts api", description = "글귀 삭제 api")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable Long id) {

        Long DeletedPostId = postsService.removeWithReplies(id);

        return responseServiceImpl.successResult(SuccessCode.DELETE_POSTS_SUCCESS, DeletedPostId);
    }

    // 게시물 1개 조회
    @Operation(summary = "select posts api", description = "글귀 검색 api")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> findAllPostsOrderByIdDesc(@PathVariable long id) {

        PostsDto postsDto = postsService.get(id);

        return responseServiceImpl.successResult(SuccessCode.SELECT_POSTS_SUCCESS, postsDto);
    }

    // 전체 게시물 내림차순 조회
    @Operation(summary = "select all posts api", description = "모든 글귀 검색 api. request 받은 페이지 기준으로 메인 화면에서 글귀를 최신 순으로 페이지당 size개씩 조회.")
//    @GetMapping("/page/{page}")
//    public ResponseEntity<ResponseDto> findAllPostsOrderByIdDesc(@PathVariable int page,
//            @ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
    @GetMapping("/list")
    public ResponseEntity<ResponseDto> findAllPostsOrderByIdDesc(@RequestParam int page,
        @RequestParam int size) {
//
//        List<PostsListResponseDto> posts = postsService.findAllPostsOrderByIdDesc(
//                page, userDetailsImpl.getMember().getId());
        List<PostsDto> posts = postsService.findAllPostsOrderByIdDesc(page, size);

        return responseServiceImpl.successResult(SuccessCode.SELECT_ALL_POSTS_SUCCESS, posts);
    }

    // 검색 내용 포함 게시물 내림차순 조회
//    @Operation(summary = "select posts containing searched content api", description = "검색된 type(c=content, a=author, t=topic)과 keyword를 포함한 게시물 조회 api. 검색 화면에서 글귀를 최신 순으로 페이지당 size개씩 조회.")
//    @GetMapping("/type/{type}/keyword/{keyword}/page/{page}")
//    public ResponseEntity<ResponseDto> findAllPostsBySearch(
//            @ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
//            @PathVariable int page, @PathVariable String keyword, @PathVariable String type) {
    @Operation(summary = "search posts api", description = "검색된 type(c=content, a=author, t=topic)과 keyword를 포함한 게시물 조회 api. 검색 화면에서 글귀를 최신 순으로 페이지당 size개씩 조회.")
    @GetMapping("/search")
    public ResponseEntity<ResponseDto> findAllPostsBySearch(@RequestParam int page,
        @RequestParam int size, @RequestParam String type, @RequestParam String keyword) {

//        List<PostsListResponseDto> posts = postsService.findAllPostsBySearch(
//                page, keyword, type, userDetailsImpl.getMember().getId());
        List<PostsDto> posts = postsService.findAllPostsBySearch(page, size, keyword, type);

        return responseServiceImpl.successResult(
            SuccessCode.SELECT_POSTS_SEARCH_SUCCESS, posts);
    }

    @ApiOperation(value = "이미지 텍스트 추출 api", notes = "이미지를 전송해 텍스트를 추출한다.")
    @PostMapping(value = "/img-extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> imageExtract(@Valid ExtractWordDto extractWordDto) {
        MultipartFile file = extractWordDto.getFile();

        String imageUrl = postsService.postsImgUpload(file, postsService.getFileUuid());
        String result = postsService.postsImgExtractWords(file, imageUrl);

        return responseServiceImpl.successResult(SuccessCode.EXTRACT_SUCCESS, result);
    }

    @ApiOperation(value = "게시글 좋아요 api", notes = "id는 게시글의 아이디이다.")
    @GetMapping(value = "/like/{id}")
    public ResponseEntity<ResponseDto> likePost(@PathVariable("id") Long id,
            @ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postsService.likePost(userDetailsImpl, id);
    }

    @ApiOperation(value = "게시글 좋아요 취소 api", notes = "id는 게시글의 아이디이다.")
    @DeleteMapping(value = "/like/{id}")
    public ResponseEntity<ResponseDto> disLikePost(@PathVariable("id") Long id,
            @ApiIgnore @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return postsService.disLikePost(userDetailsImpl, id);
    }
}
