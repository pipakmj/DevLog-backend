package com.devlog.devlog.auth.dto.admin;

import com.devlog.devlog.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private int id;
    private String email;
    private String nickname;
    private String githubUrl;
    private String role;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(UserEntity user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .githubUrl(user.getGithub_url())
                .role(user.getRole().name())
                .createdAt(user.getCreated_at())
                .build();
    }
}
