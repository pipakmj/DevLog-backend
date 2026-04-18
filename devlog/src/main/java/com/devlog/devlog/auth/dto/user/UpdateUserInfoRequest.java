package com.devlog.devlog.auth.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserInfoRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;
    private String bio;
    private String github_url;
}
