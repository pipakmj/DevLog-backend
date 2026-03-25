package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.SignInRequest;
import com.devlog.devlog.auth.dto.SignUpRequest;
import com.devlog.devlog.auth.dto.UserResponse;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.service.UserService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@RequestBody @Valid SignUpRequest request) {
        userService.signUp(request);
        UserResponse data = UserResponse.from(request.getEmail(), request.getNickname());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 성공적으로 완료되었습니다.", data));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<UserResponse>> signIn(@RequestBody @Valid SignInRequest request) {
        UserEntity user = userService.signIn(request);
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        userService.saveRefreshToken(user.getEmail(), refreshToken);
        UserResponse data = UserResponse.loginSuccess(user, accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("로그인이 성공적으로 완료되었습니다.", data));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<Void>> signOut(Authentication authentication) {
        if (authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("이미 로그아웃 상태입니다", null));
        }
        String email = authentication.getName();
        userService.signOut(email);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("성공적으로 로그아웃 되었습니다", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = userService.refreshAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("성공적으로 갱신되었습니다", Map.of("access_token", newAccessToken)));
    }
}
