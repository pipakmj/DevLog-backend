package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.PostRequest;
import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.service.PostService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse> createPost(Authentication authentication, @RequestBody PostRequest postRequest) {
        String userEmail = authentication.getName();
        postService.createPost(userEmail, postRequest);
        return ResponseEntity.ok(ApiResponse.success("포스트가 성공적으로 저장되었습니다."));
    }
}
