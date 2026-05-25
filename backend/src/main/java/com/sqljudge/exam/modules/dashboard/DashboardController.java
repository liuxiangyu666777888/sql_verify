package com.sqljudge.exam.modules.dashboard;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/student/dashboard")
    public ApiResponse<Map<String, Object>> student() {
        return ApiResponse.ok(dashboardService.student());
    }

    @GetMapping("/api/teacher/dashboard")
    public ApiResponse<Map<String, Object>> teacher() {
        return ApiResponse.ok(dashboardService.teacher());
    }
}
