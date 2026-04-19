package com.devlog.devlog.auth.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private Long parentId;
    @NotBlank(message = "댓글을 입력해주십시오.")
    private String content;
}
