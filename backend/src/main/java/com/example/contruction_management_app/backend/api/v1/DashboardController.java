package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.DashboardSummaryDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
        @RequestParam(required = false) String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        DashboardSummaryDto summary = dashboardService.getSummary(projectId, principal);
        return ResponseEntity.ok(summary);
    }
}
