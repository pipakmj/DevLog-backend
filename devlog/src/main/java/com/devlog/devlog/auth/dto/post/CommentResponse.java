package com.devlog.devlog.auth.dto.post;

import com.devlog.devlog.auth.entity.CommentEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isDeleted")
    private boolean isDeleted;

    public static CommentResponse getPostComments(CommentEntity commentEntity) {
        return CommentResponse.builder()
                .commentId(commentEntity.getId())
                .content(commentEntity.isDeleted() ? "삭제된 댓글입니다." : commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .parentId(commentEntity.getParentId())
                .isDeleted(commentEntity.isDeleted())
                .nickname(commentEntity.isDeleted() ? "알 수 없음" : commentEntity.getUserEntity().getNickname())
                .build();
    }
}
