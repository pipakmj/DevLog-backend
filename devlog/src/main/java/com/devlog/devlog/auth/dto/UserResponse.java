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
    private String token;

    public static UserResponse from(String email, String nickname) {
        return UserResponse.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }

    public static UserResponse loginSuccess(UserEntity user, String token) {
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .token(token)
                .build();
    }
}
