package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.auth.SignInRequest;
import com.devlog.devlog.auth.dto.auth.SignUpRequest;
import com.devlog.devlog.auth.dto.user.UserResponse;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.service.UserService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<UserResponse>> signIn(
            @RequestBody @Valid SignInRequest request,
            HttpServletResponse response
    ) {
        UserEntity user = userService.signIn(request);
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        userService.saveRefreshToken(user.getEmail(), refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)// 테스트환경(http)에서 false, 배포(https)시 true
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        UserResponse data = UserResponse.loginSuccess(user, accessToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("로그인이 성공적으로 완료되었습니다.", data));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse<Void>> signOut(
            Authentication authentication,
            HttpServletResponse response
    ) {
        if (authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("이미 로그아웃 상태입니다", null));
        }
        userService.signOut(authentication.getName());
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("성공적으로 로그아웃 되었습니다", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if(refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String newAccessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("성공적으로 갱신되었습니다", Map.of("access_token", newAccessToken)));
    }
}
