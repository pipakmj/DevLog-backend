package com.devlog.devlog.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private Long projectId;
    private String tags;
}
