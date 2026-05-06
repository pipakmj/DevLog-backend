package com.devlog.devlog.auth.service;

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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

    private final Cache<String, String> CacheStore = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .bio(request.getBio())
                .github_url(request.getGithub_url())
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public void signUpSendCode(String email) {
        String code = valificationCodeGenerator.generate();
        CacheStore.put(email, code);
        mailService.send(email, code);
    }

    public Boolean signUpVerifyCode(String email, String code) {
        String savedCode = CacheStore.getIfPresent(email);
        if (savedCode != null && savedCode.equals(code)) {
            CacheStore.invalidate(email);
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
        CacheStore.put(email, code);
        mailService.send(email, code);
    }

    public String validateCode(String email, String code ) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String savedCode = CacheStore.getIfPresent(email);
        if (savedCode != null && savedCode.equals(code)) {
            CacheStore.invalidate(email);
            String token = UUID.randomUUID().toString();
            CacheStore.put(token, email);
            return token;
        }
        return "";
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = CacheStore.getIfPresent(token);
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        CacheStore.invalidate(token);
    }

    @Transactional
    public void signOut(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(14);

        refreshTokenRepository.findByEmail(email).ifPresentOrElse(entity -> {
            entity.setToken(refreshToken);
            entity.setExpiryTime(expiryTime);
        },
                () -> refreshTokenRepository.save(RefreshTokenEntity.builder()
                        .email(email)
                        .token(refreshToken)
                        .expiryTime(expiryTime)
                        .createAt(LocalDateTime.now())
                        .build()));
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (tokenEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(tokenEntity);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }
        return jwtTokenProvider.createAccessToken(tokenEntity.getEmail());
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
