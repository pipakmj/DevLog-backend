package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.dashboard.DashboardKpiResponse;
import com.devlog.devlog.auth.service.DashboardService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashBoardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getKpiData() {
        DashboardKpiResponse kpiData = dashboardService.getKpiData();
        return ResponseEntity.ok(ApiResponse.success("KPI 데이터를 성공적으로 가져왔습니다.", kpiData));
    }
}
