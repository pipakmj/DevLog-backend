package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.user.UpdateUserInfoRequest;
import com.devlog.devlog.auth.dto.user.UserResponse;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.service.UserService;
import com.devlog.devlog.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUserInfo(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.getUserInfo(email);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보를 성공적으로 가져왔습니다.", UserResponse.getUserInfoResponse(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            Authentication authentication,
            @RequestBody @Valid UpdateUserInfoRequest request) {
        String email = authentication.getName();
        userService.updateUserInfo(email, request);
        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 성공적으로 수정되었습니다."));
    }
}
