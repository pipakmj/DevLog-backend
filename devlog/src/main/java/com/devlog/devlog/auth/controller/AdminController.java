package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.admin.AdminUserResponse;
import com.devlog.devlog.auth.dto.post.PostResponse;
import com.devlog.devlog.auth.dto.project.ProjectResponse;
import com.devlog.devlog.auth.service.AdminService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.common.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<CustomPageResponse<AdminUserResponse>>> getUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("관리자 사용자 목록 조회에 성공했습니다.", adminService.getUsers(pageable)));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<CustomPageResponse<ProjectResponse>>> getProjects(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("관리자 프로젝트 목록 조회에 성공했습니다.", adminService.getProjects(pageable)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<CustomPageResponse<PostResponse>>> getPosts(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("관리자 TIL 목록 조회에 성공했습니다.", adminService.getPosts(pageable)));
    }
}
