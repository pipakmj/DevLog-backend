package com.devlog.devlog.auth.dto.post;

import com.devlog.devlog.auth.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private String nickname;
    private Long parentId;

    public static CommentResponse getPostComments(CommentEntity commentEntity) {
        return CommentResponse.builder()
                .commentId(commentEntity.getId())
                .content(commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .parentId(commentEntity.getParentId())
                .nickname(commentEntity.getUserEntity().getNickname())
                .build();
    }
}
