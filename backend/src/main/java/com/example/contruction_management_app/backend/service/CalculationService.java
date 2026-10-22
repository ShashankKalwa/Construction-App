package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Calculation;
import com.example.contruction_management_app.backend.domain.CalculatorType;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.dto.CalculationResponseDto;
import com.example.contruction_management_app.backend.dto.CalculationSaveRequest;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.CalculationRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CalculationService {

    private final CalculationRepository calculationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public CalculationService(
        CalculationRepository calculationRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.calculationRepository = calculationRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public CalculationResponseDto saveCalculation(CalculationSaveRequest request, UserPrincipal principal) {
        validateProjectAccess(request.projectId(), principal);

        String id = "calc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Calculation calculation = Calculation.builder()
            .id(id)
            .projectId(request.projectId())
            .calculatorType(request.calculatorType())
            .title(request.title())
            .elementType(request.elementType())
            .inputParameters(request.inputParameters())
            .outputResults(request.outputResults())
            .primaryQuantity(request.primaryQuantity())
            .primaryUnit(request.primaryUnit())
            .createdBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        Calculation saved = calculationRepository.save(calculation);
        return CalculationResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CalculationResponseDto> getCalculationsForProject(
        String projectId,
        CalculatorType type,
        UserPrincipal principal
    ) {
        validateProjectAccess(projectId, principal);

        List<Calculation> calculations;
        if (type != null) {
            calculations = calculationRepository.findByProjectIdAndCalculatorTypeOrderByCreatedAtDesc(projectId, type);
        } else {
            calculations = calculationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }

        return calculations.stream()
            .map(CalculationResponseDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public CalculationResponseDto getCalculationById(String id, UserPrincipal principal) {
        Calculation calculation = calculationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Calculation record not found: " + id));

        validateProjectAccess(calculation.getProjectId(), principal);
        return CalculationResponseDto.fromEntity(calculation);
    }

    private Project validateProjectAccess(String projectId, UserPrincipal principal) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        if (!project.getOrganization().getId().equals(principal.getOrganizationId())) {
            throw new ForbiddenException("Access denied to project from another organization");
        }

        if (!"super_admin".equals(principal.getRoleId()) && !"company_admin".equals(principal.getRoleId())) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.getId());
            if (!isMember) {
                throw new ForbiddenException("Access denied. You are not a member of project " + projectId);
            }
        }
        return project;
    }
}
