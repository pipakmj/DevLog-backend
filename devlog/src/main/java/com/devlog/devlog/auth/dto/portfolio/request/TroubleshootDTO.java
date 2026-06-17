package com.devlog.devlog.auth.dto.portfolio.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TroubleshootDTO {
    private String issue;
    private String resolution;
}
