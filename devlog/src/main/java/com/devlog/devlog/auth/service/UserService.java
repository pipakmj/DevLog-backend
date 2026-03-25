package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.SignInRequest;
import com.devlog.devlog.auth.dto.SignUpRequest;
import com.devlog.devlog.auth.entity.RefreshTokenEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.RefreshTokenRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    public UserEntity signIn(SignInRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return user;
    }

    @Transactional
    public void signOut(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    @Transactional
    public void saveRefreshToken(String email, String refreshToken) {
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(14);

        refreshTokenRepository.findByEmail(email).
                ifPresentOrElse(entity -> {
                    entity.setToken(refreshToken);
                    entity.setExpiryTime(expiryTime);
                },
                () -> refreshTokenRepository.save(RefreshTokenEntity.builder()
                        .email(email)
                        .token(refreshToken)
                        .expiryTime(expiryTime)
                        .createAt(LocalDateTime.now())
                        .build())
                );
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
}
