package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.BoqItem;
import com.example.contruction_management_app.backend.domain.Measurement;
import com.example.contruction_management_app.backend.domain.Project;
import com.example.contruction_management_app.backend.dto.MeasurementCreateDto;
import com.example.contruction_management_app.backend.dto.MeasurementDto;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.BoqItemRepository;
import com.example.contruction_management_app.backend.repository.MeasurementRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final BoqItemRepository boqItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public MeasurementService(
        MeasurementRepository measurementRepository,
        BoqItemRepository boqItemRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.measurementRepository = measurementRepository;
        this.boqItemRepository = boqItemRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public MeasurementDto recordMeasurement(String projectId, MeasurementCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        BoqItem boqItem = null;
        if (dto.boqItemId() != null && !dto.boqItemId().isBlank()) {
            boqItem = boqItemRepository.findById(dto.boqItemId()).orElse(null);
        }

        // Calculate gross quantity: Units x Length x Width x Depth
        BigDecimal gross = dto.numberOfUnits();
        if (dto.lengthM() != null && dto.lengthM().compareTo(BigDecimal.ZERO) > 0) {
            gross = gross.multiply(dto.lengthM());
        }
        if (dto.widthM() != null && dto.widthM().compareTo(BigDecimal.ZERO) > 0) {
            gross = gross.multiply(dto.widthM());
        }
        if (dto.depthM() != null && dto.depthM().compareTo(BigDecimal.ZERO) > 0) {
            gross = gross.multiply(dto.depthM());
        }

        BigDecimal deduction = dto.deductionM3() != null ? dto.deductionM3() : BigDecimal.ZERO;
        BigDecimal netQuantity = gross.subtract(deduction).max(BigDecimal.ZERO);

        String id = "meas_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Measurement measurement = Measurement.builder()
            .id(id)
            .project(project)
            .boqItem(boqItem)
            .itemDescription(dto.itemDescription())
            .numberOfUnits(dto.numberOfUnits())
            .lengthM(dto.lengthM())
            .widthM(dto.widthM())
            .depthM(dto.depthM())
            .deductionM3(deduction)
            .netQuantity(netQuantity)
            .unit(dto.unit())
            .locationDetails(dto.locationDetails())
            .measuredBy(principal.getId())
            .measuredAt(Instant.now())
            .build();

        Measurement saved = measurementRepository.save(measurement);
        return MeasurementDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MeasurementDto> getMeasurementsForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return measurementRepository.findByProjectIdOrderByMeasuredAtDesc(projectId).stream()
            .map(MeasurementDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MeasurementDto> getMeasurementsForBoqItem(String boqItemId, UserPrincipal principal) {
        BoqItem boqItem = boqItemRepository.findById(boqItemId)
            .orElseThrow(() -> new ResourceNotFoundException("BOQ item not found: " + boqItemId));

        validateProjectAccess(boqItem.getBoq().getProject().getId(), principal);
        return measurementRepository.findByBoqItemIdOrderByMeasuredAtDesc(boqItemId).stream()
            .map(MeasurementDto::fromEntity)
            .toList();
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
