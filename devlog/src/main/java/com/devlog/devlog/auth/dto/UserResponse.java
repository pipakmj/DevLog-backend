package com.devlog.devlog.auth.dto;

import com.devlog.devlog.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String email;
    private String nickname;
    private String bio;
    private String github_url;
    private String accessToken;
    private String refreshToken;

    public static UserResponse from(String email, String nickname) {
        return UserResponse.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }

    public static UserResponse loginSuccess(UserEntity user, String accessToken) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(accessToken)
                .build();
    }

    public static UserResponse getUserInfoResponse(UserEntity userEntity) {
        return UserResponse.builder()
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .bio(userEntity.getBio())
                .github_url(userEntity.getGithub_url())
                .build();
    }
}
