package com.devlog.devlog.auth.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    String nickname;

    String bio;
    String github_url;
}
