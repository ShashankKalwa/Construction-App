package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.ProjectCreateDto;
import com.example.contruction_management_app.backend.dto.ProjectDetailDto;
import com.example.contruction_management_app.backend.dto.ProjectMemberAssignDto;
import com.example.contruction_management_app.backend.dto.ProjectMemberDto;
import com.example.contruction_management_app.backend.dto.ProjectResponseDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> listProjects(@AuthenticationPrincipal UserPrincipal principal) {
        List<ProjectResponseDto> projects = projectService.listProjects(principal);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('projects:create') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ProjectResponseDto> createProject(
        @Valid @RequestBody ProjectCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectResponseDto created = projectService.createProject(dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProject(
        @PathVariable String id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectResponseDto project = projectService.getProject(id, principal);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ProjectDetailDto> getProjectDetail(
        @PathVariable String id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectDetailDto detail = projectService.getProjectDetail(id, principal);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('projects:edit') or hasRole('SUPER_ADMIN') or hasRole('COMPANY_ADMIN')")
    public ResponseEntity<ProjectMemberDto> assignMember(
        @PathVariable String id,
        @Valid @RequestBody ProjectMemberAssignDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProjectMemberDto member = projectService.assignMember(id, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }
}
