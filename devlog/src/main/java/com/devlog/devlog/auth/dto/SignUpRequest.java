package com.devlog.devlog.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @NotNull
    String email;
    String password;
    String nickname;
    String bio;
    String github_url;
}
