package com.devlog.devlog.auth.dto;

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

    public static UserResponse from(String email, String nickname) {
        return UserResponse.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}
