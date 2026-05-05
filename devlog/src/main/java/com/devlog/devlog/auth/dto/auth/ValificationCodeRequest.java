package com.devlog.devlog.auth.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ValificationCodeRequest {
    @NotBlank(message = "인증코드는 필수 입력 항목입니다.")
    private String code;
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
}
