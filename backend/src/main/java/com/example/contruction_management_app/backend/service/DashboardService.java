package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.dto.DashboardSummaryDto;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.repository.TaskRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(String projectId, UserPrincipal principal) {
        List<Project> projects;
        if ("super_admin".equals(principal.getRoleId()) || "company_admin".equals(principal.getRoleId())) {
            projects = projectRepository.findAllByOrganizationId(principal.getOrganizationId());
        } else {
            projects = projectRepository.findAllByOrganizationAndUser(principal.getOrganizationId(), principal.getId());
        }

        BigDecimal totalBudget = projects.stream()
            .map(Project::getBudgetInr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgProgress = projects.isEmpty() ? 0.0 :
            projects.stream().mapToDouble(Project::getProgressPct).average().orElse(0.0);

        String targetProjectId = (projectId != null && !projectId.isBlank()) ? projectId :
            (projects.isEmpty() ? null : projects.get(0).getId());

        int pending = 0;
        int inProgress = 0;
        int blocked = 0;
        int completed = 0;

        if (targetProjectId != null) {
            pending = (int) taskRepository.countByProjectIdAndStatus(targetProjectId, "PENDING");
            inProgress = (int) taskRepository.countByProjectIdAndStatus(targetProjectId, "IN_PROGRESS");
            blocked = (int) taskRepository.countByProjectIdAndStatus(targetProjectId, "BLOCKED");
            completed = (int) taskRepository.countByProjectIdAndStatus(targetProjectId, "COMPLETED");
        }

        Map<String, String> roleMetrics = new LinkedHashMap<>();
        String roleId = principal.getRoleId();

        if ("project_manager".equals(roleId) || "company_admin".equals(roleId) || "super_admin".equals(roleId)) {
            roleMetrics.put("Active Projects", String.valueOf(projects.size()));
            roleMetrics.put("Total Budget", String.format("₹%.2f Cr", totalBudget.doubleValue() / 10000000.0));
            roleMetrics.put("Avg Progress", ((int) avgProgress) + "%");
            roleMetrics.put("Blocked Tasks", String.valueOf(blocked));
        } else if ("site_supervisor".equals(roleId)) {
            roleMetrics.put("Today's Tasks", String.valueOf(pending + inProgress));
            roleMetrics.put("In Progress", String.valueOf(inProgress));
            roleMetrics.put("Blocked Issues", String.valueOf(blocked));
            roleMetrics.put("Labor Present", "28 on-site");
        } else if ("civil_engineer".equals(roleId)) {
            roleMetrics.put("Active Phases", "2 Ongoing");
            roleMetrics.put("Inspections Due", String.valueOf(inProgress));
            roleMetrics.put("Calculations Log", "14 Run");
            roleMetrics.put("BOQ Status", "Verified");
        } else if ("accountant".equals(roleId)) {
            roleMetrics.put("Committed Budget", String.format("₹%.2f Cr", totalBudget.doubleValue() / 10000000.0));
            roleMetrics.put("Spend Actual", "₹87.5 L (35%)");
            roleMetrics.put("Pending Invoices", "3 Bills");
            roleMetrics.put("Variance", "+1.8%");
        } else {
            roleMetrics.put("Assigned Work", String.valueOf(pending + inProgress));
            roleMetrics.put("Completed", String.valueOf(completed));
            roleMetrics.put("Project Status", projects.isEmpty() ? "None" : "Active");
            roleMetrics.put("Due This Week", String.valueOf(inProgress));
        }

        return new DashboardSummaryDto(
            projects.size(),
            totalBudget,
            avgProgress,
            pending,
            inProgress,
            blocked,
            completed,
            roleMetrics
        );
    }
}
