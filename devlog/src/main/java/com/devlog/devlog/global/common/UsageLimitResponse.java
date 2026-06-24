package com.devlog.devlog.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsageLimitResponse {
    private int dailyLimit;
    private int used;
    private int remaining;
    private String resetAt;
}
