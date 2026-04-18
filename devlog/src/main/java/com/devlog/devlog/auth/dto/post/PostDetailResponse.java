package com.devlog.devlog.auth.dto.post;

import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.entity.TagEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime date;
    private List<String> tags;
    private String projectName;
    private Long projectId;
    private int views;

    public static PostDetailResponse from(PostEntity post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getUser().getNickname())
                .date(post.getCreated_at())
                .tags(post.getTags().stream()
                        .map(TagEntity::getName)
                        .collect(Collectors.toList()))
                .projectName(post.getProject().getTitle())
                .projectId(post.getProject().getId())
                .views(post.getViews())
                .build();
    }
}
