package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BoqService {

    private final BoqRepository boqRepository;
    private final BoqItemRepository boqItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public BoqService(
        BoqRepository boqRepository,
        BoqItemRepository boqItemRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.boqRepository = boqRepository;
        this.boqItemRepository = boqItemRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public BoqDto createBoq(String projectId, BoqCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        int nextVersion = boqRepository.findFirstByProjectIdOrderByVersionDesc(projectId)
            .map(b -> b.getVersion() + 1)
            .orElse(1);

        String id = "boq_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Boq boq = Boq.builder()
            .id(id)
            .project(project)
            .title(dto.title())
            .version(nextVersion)
            .status(BoqStatus.DRAFT)
            .totalAmount(BigDecimal.ZERO)
            .createdBy(principal.getId())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Boq saved = boqRepository.save(boq);
        return BoqDto.fromEntity(saved);
    }

    @Transactional
    public BoqItemDto addBoqItem(String boqId, BoqItemCreateDto dto, UserPrincipal principal) {
        Boq boq = boqRepository.findById(boqId)
            .orElseThrow(() -> new ResourceNotFoundException("BOQ not found: " + boqId));

        validateProjectAccess(boq.getProject().getId(), principal);

        if (boq.getStatus() == BoqStatus.APPROVED) {
            throw new BadRequestException("Cannot add items to an approved BOQ. Create a new revision version.");
        }

        BigDecimal totalAmount = dto.estimatedQuantity().multiply(dto.unitRate());
        int orderIndex = boq.getItems().size() + 1;
        String itemId = "boqi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        BoqItem item = BoqItem.builder()
            .id(itemId)
            .boq(boq)
            .itemCode(dto.itemCode())
            .description(dto.description())
            .category(dto.category())
            .unit(dto.unit())
            .estimatedQuantity(dto.estimatedQuantity())
            .unitRate(dto.unitRate())
            .totalAmount(totalAmount)
            .calculationId(dto.calculationId())
            .orderIndex(orderIndex)
            .build();

        BoqItem savedItem = boqItemRepository.save(item);

        // Recalculate and update BOQ total amount
        BigDecimal newBoqTotal = boq.getItems().stream()
            .map(BoqItem::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(totalAmount);

        boq.setTotalAmount(newBoqTotal);
        boq.setUpdatedAt(Instant.now());
        boqRepository.save(boq);

        return BoqItemDto.fromEntity(savedItem);
    }

    @Transactional
    public BoqDto submitBoq(String boqId, UserPrincipal principal) {
        Boq boq = boqRepository.findById(boqId)
            .orElseThrow(() -> new ResourceNotFoundException("BOQ not found: " + boqId));

        validateProjectAccess(boq.getProject().getId(), principal);

        if (boq.getStatus() != BoqStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT BOQs can be submitted for approval.");
        }

        boq.setStatus(BoqStatus.SUBMITTED);
        boq.setUpdatedAt(Instant.now());
        Boq saved = boqRepository.save(boq);
        return BoqDto.fromEntity(saved);
    }

    @Transactional
    public BoqDto decideBoq(String boqId, BoqDecisionDto decisionDto, UserPrincipal principal) {
        Boq boq = boqRepository.findById(boqId)
            .orElseThrow(() -> new ResourceNotFoundException("BOQ not found: " + boqId));

        validateProjectAccess(boq.getProject().getId(), principal);

        if (boq.getStatus() != BoqStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED BOQs can be reviewed.");
        }

        // Self-Approval Security Invariant
        if (principal.getId().equals(boq.getCreatedBy())) {
            throw new ForbiddenException("Self-approval is forbidden. A creator cannot approve their own BOQ.");
        }

        BoqStatus targetStatus = BoqStatus.valueOf(decisionDto.decision());
        boq.setStatus(targetStatus);
        boq.setApprovedBy(principal.getId());
        boq.setUpdatedAt(Instant.now());

        Boq saved = boqRepository.save(boq);
        return BoqDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<BoqDto> getBoqsForProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return boqRepository.findByProjectIdOrderByVersionDesc(projectId).stream()
            .map(BoqDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public BoqDto getBoqById(String boqId, UserPrincipal principal) {
        Boq boq = boqRepository.findById(boqId)
            .orElseThrow(() -> new ResourceNotFoundException("BOQ not found: " + boqId));

        validateProjectAccess(boq.getProject().getId(), principal);
        return BoqDto.fromEntity(boq);
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
