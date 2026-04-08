package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.ProjectRequest;
import com.devlog.devlog.auth.dto.ProjectResponse;
import com.devlog.devlog.auth.service.ProjectService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @PageableDefault(size = 9, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        List<ProjectResponse> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 모든 프로젝트 정보를 가져왔습니다", projects));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjects(
            Authentication authentication,
            @PageableDefault(size = 9, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String userEmail = authentication.getName();
        List<ProjectResponse> projects = projectService.getUserProjects(userEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 프로젝트 정보를 가져왔습니다.", projects));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            Authentication authentication,
            @RequestBody ProjectRequest request) {
        String userEmail = authentication.getName();
        ProjectResponse project = projectService.createProject(userEmail, request);
        return ResponseEntity.ok(ApiResponse.success("프로젝트가 성공적으로 생성되었습니다.", project));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ProjectRequest request) {
        String userEmail = authentication.getName();
        ProjectResponse project = projectService.updateProject(id, userEmail, request);
        return ResponseEntity.ok(ApiResponse.success("프로젝트가 성공적으로 수정되었습니다.", project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            Authentication authentication,
            @PathVariable Long id) {
        String userEmail = authentication.getName();
        projectService.deleteProject(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("프로젝트가 성공적으로 삭제되었습니다."));
    }
}
