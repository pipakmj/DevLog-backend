package com.devlog.devlog.auth.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiResponse {
    private long totalUsers;
    private long totalProjects;
    private long totalPosts; // Using this for Portfolio count for now
    private long unprocessedReports;
}
