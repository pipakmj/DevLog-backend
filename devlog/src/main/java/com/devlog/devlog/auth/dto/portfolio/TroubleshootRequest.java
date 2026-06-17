package com.devlog.devlog.auth.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootRequest {
    private String issue;
    private String resolution;
}
