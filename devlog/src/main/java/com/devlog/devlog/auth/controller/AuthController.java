package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.SignUpRequest;
import com.devlog.devlog.auth.dto.UserResponse;
import com.devlog.devlog.auth.service.UserService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@RequestBody SignUpRequest request) {
        userService.signUp(request);
        UserResponse data = UserResponse.from(request.getEmail(), request.getNickname());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 성공적으로 완료되었습니다.", data));
    }
}
