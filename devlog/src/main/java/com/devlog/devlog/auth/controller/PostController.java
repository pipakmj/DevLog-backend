package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.PostDetailResponse;
import com.devlog.devlog.auth.dto.PostRequest;
import com.devlog.devlog.auth.dto.PostResponse;
import com.devlog.devlog.auth.service.PostService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(Authentication authentication,
            @RequestBody PostRequest postRequest) {
        String userEmail = authentication.getName();
        postService.createPost(userEmail, postRequest);
        return ResponseEntity.ok(ApiResponse.success("포스트가 성공적으로 저장되었습니다."));
    }

    @PatchMapping("/edit/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            Authentication authentication,
            @PathVariable Long postId ,
            @RequestBody PostRequest postRequest) {
        String userEmail = authentication.getName();
        postService.updatePost(userEmail, postId, postRequest);
        return ResponseEntity.ok(ApiResponse.success("포스트 업데이트가 성공적으로 완료되었습니다. "));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPosts(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("포스트 목록 조회가 성공적으로 완료되었습니다.", postService.getAllPosts(pageable)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getDetailPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("상세 포스트 조회가 성공적으로 완료되었습니다.", postService.getDetailPost(postId)));
    }

    @PatchMapping("/{postId}/views")
    public ResponseEntity<ApiResponse<Void>> updatePostViewCount(@PathVariable Long postId) {
        postService.updatePostViewCount(postId);
        return ResponseEntity.ok(ApiResponse.success("조회수 업데이트가 성공적으로 완료되었습니다."));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(Authentication authentication, @PathVariable Long postId) {
        String userEmail = authentication.getName();
        postService.deletePost(userEmail, postId);
        return ResponseEntity.ok(ApiResponse.success("포스트가 성공적으로 삭제되었습니다"));
    }
}
