package com.devlog.devlog.auth.dto.github;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitAnalyzeResponse {
    private List<String> techStack;
    private String description;
    private List<String> features;
}
