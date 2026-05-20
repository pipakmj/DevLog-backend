package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.project.ProjectRequest;
import com.devlog.devlog.auth.dto.project.ProjectResponse;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.service.ProjectService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.common.CustomSliceResponse;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final ProjectRepository projectRepository;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<CustomSliceResponse<ProjectResponse>>> getAllProjects(
            @PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        CustomSliceResponse<ProjectResponse> projectResponseList = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 모든 프로젝트 정보를 가져왔습니다", projectResponseList));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<CustomSliceResponse<ProjectResponse>>> getUserProjects(
            Authentication authentication,
            @PageableDefault(size = 9, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userEmail = authentication.getName();
        CustomSliceResponse<ProjectResponse> projectResponseList = projectService.getUserProjects(userEmail, pageable);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 프로젝트 정보를 가져왔습니다.", projectResponseList));
    }

    @GetMapping("/mine/post")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjectsToPost(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ProjectResponse> projects = projectService.getUserProjectsToPost(userEmail);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 프로젝트 정보를 가져왔습니다.", projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getDetailProject(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("성공적으로 프로젝트 상세 내용을 가져왔습니다. ", projectService.getDetailProject(id)));
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
