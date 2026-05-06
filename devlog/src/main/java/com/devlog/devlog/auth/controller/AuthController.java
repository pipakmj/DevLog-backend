package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.auth.ResetPasswordRequest;
import com.devlog.devlog.auth.dto.auth.SignInRequest;
import com.devlog.devlog.auth.dto.auth.SignUpRequest;
import com.devlog.devlog.auth.dto.auth.ValificationCodeRequest;
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

    @PostMapping("/signup/send-code")
    public ResponseEntity<ApiResponse<Void>> signUpSendCode(@RequestParam String email) {
        userService.signUpSendCode(email);
        return ResponseEntity.ok(ApiResponse.success("인증번호가 성공적으로 전송되었습니다."));
    }

    @PostMapping("/signup/verify-code")
    public ResponseEntity<ApiResponse<Boolean>> signUpVerifyCode(@RequestBody ValificationCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        return ResponseEntity.ok(ApiResponse.success("인증이 성공적으로 완료되었습니다.", userService.signUpVerifyCode(email, code)));
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
                .secure(true)// 테스트환경(http)에서 false, 배포(https)시 true
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("None")
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

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 찾기가 성공적으로 완료되었습니다."));
    }

    @PostMapping("/password/code")
    public ResponseEntity<ApiResponse<String>> validateCode(@RequestBody @Valid ValificationCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        return ResponseEntity.ok(ApiResponse.success("인증되었습니다.", userService.validateCode(email, code)));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정이 성공적으로 완료되었습니다."));
    }
}
