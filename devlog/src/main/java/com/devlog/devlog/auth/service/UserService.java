package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.SignUpRequest;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequest request) {
        // 이미 가입된 이메일인지 체크 (옵션: 테스트 시 우선순위는 낮지만 추가해 둠)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 유저입니다.");
        }

        // 비밀번호 암호화 후 저장
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
}
