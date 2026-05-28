package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.UserRole;
import com.devlog.devlog.auth.dto.auth.SignInRequest;
import com.devlog.devlog.auth.dto.auth.SignUpRequest;
import com.devlog.devlog.auth.dto.auth.ValificationCodeRequest;
import com.devlog.devlog.auth.dto.user.UpdateUserInfoRequest;
import com.devlog.devlog.auth.entity.RefreshTokenEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.RefreshTokenRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.ValificationCodeGenerator;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ValificationCodeGenerator valificationCodeGenerator;
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String verified = redisTemplate.opsForValue().get(request.getEmail() + "_VERIFIED");
        if (!"true".equals(verified)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다."); // 필요시 ErrorCode 추가
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .bio(request.getBio())
                .github_url(request.getGithub_url())
                .role(UserRole.ROLE_USER)
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        userRepository.save(user);
        redisTemplate.delete(request.getEmail() + "_VERIFIED");
    }

    public void signUpSendCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        String code = valificationCodeGenerator.generate();
        redisTemplate.opsForValue().set(email, code, 10, TimeUnit.MINUTES);
        mailService.send(email, code);
    }

    public Boolean signUpVerifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(email);
            // 30분 동안 회원가입을 완료할 수 있도록 세팅
            redisTemplate.opsForValue().set(email + "_VERIFIED", "true", 30, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }

    public UserEntity signIn(SignInRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String code = valificationCodeGenerator.generate();
        redisTemplate.opsForValue().set(email, code, 10, TimeUnit.MINUTES);
        mailService.send(email, code);
    }

    public String validateCode(String email, String code) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String savedCode = redisTemplate.opsForValue().get(email);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(email);
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(token, email, 10, TimeUnit.MINUTES);
            return token;
        }
        return "";
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get(token);
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redisTemplate.delete(token);
    }

    public void signOut(String email) {
        redisTemplate.delete("RT:" + email);
    }

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set("RT:" + email, refreshToken, 14, TimeUnit.DAYS);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String useremail = jwtTokenProvider.getUserId(refreshToken);
        if (useremail == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + useremail);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        return jwtTokenProvider.createAccessToken(useremail);
    }

    @Transactional(readOnly = true)
    public UserEntity getUserInfo(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUserInfo(String email, UpdateUserInfoRequest request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setNickname(request.getNickname());
        user.setBio(request.getBio());
        user.setGithub_url(request.getGithub_url());
        user.setUpdated_at(LocalDateTime.now());

        userRepository.save(user);
    }
}
