package com.devlog.devlog.auth.dto.github;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GitAnalyzeRequest {
    private String gitUrl;
}
