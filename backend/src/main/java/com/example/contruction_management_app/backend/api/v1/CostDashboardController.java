package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.CostDashboardSummaryDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.CostDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CostDashboardController {

    private final CostDashboardService costDashboardService;

    public CostDashboardController(CostDashboardService costDashboardService) {
        this.costDashboardService = costDashboardService;
    }

    @GetMapping("/projects/{projectId}/cost-dashboard")
    public ResponseEntity<CostDashboardSummaryDto> getCostDashboard(
        @PathVariable String projectId,
        @RequestParam(defaultValue = "TOTAL") String period,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(costDashboardService.getCostDashboardSummary(projectId, period, principal));
    }
}
