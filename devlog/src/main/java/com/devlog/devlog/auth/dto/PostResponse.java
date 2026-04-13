package com.devlog.devlog.auth.dto;

import com.devlog.devlog.auth.entity.PostEntity;
import com.devlog.devlog.auth.entity.TagEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime date;
    private List<String> tags;
    private int views;
    private String projectName;

    public static PostResponse from(PostEntity post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(post.getUser().getNickname())
                .date(post.getCreated_at())
                .tags(post.getTags().stream()
                        .map(TagEntity::getName)
                        .collect(Collectors.toList()))
                .views(post.getViews())
                .projectName(post.getProject().getTitle())
                .build();
    }
}
