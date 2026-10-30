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
public class InventoryService {

    private final MaterialRepository materialRepository;
    private final StockMovementRepository stockMovementRepository;
    private final MaterialRequestRepository materialRequestRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public InventoryService(
        MaterialRepository materialRepository,
        StockMovementRepository stockMovementRepository,
        MaterialRequestRepository materialRequestRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.materialRepository = materialRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.materialRequestRepository = materialRequestRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> getMaterialsByProject(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return materialRepository.findByProjectIdOrderByNameAsc(projectId).stream()
            .map(MaterialDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public MaterialDto getMaterial(String projectId, String materialId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        Material m = materialRepository.findById(materialId)
            .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));
        return MaterialDto.fromEntity(m);
    }

    @Transactional
    public MaterialDto createMaterial(String projectId, MaterialCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        if (materialRepository.findByProjectIdAndCode(projectId, dto.code()).isPresent()) {
            throw new BadRequestException("Material with code '" + dto.code() + "' already exists in this project");
        }

        String matId = "mat_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        BigDecimal initialStock = dto.initialStock() != null ? dto.initialStock() : BigDecimal.ZERO;
        BigDecimal minThreshold = dto.minStockThreshold() != null ? dto.minStockThreshold() : BigDecimal.ZERO;
        BigDecimal unitCost = dto.unitCost() != null ? dto.unitCost() : BigDecimal.ZERO;

        Material material = Material.builder()
            .id(matId)
            .project(project)
            .code(dto.code())
            .name(dto.name())
            .category(dto.category())
            .unit(dto.unit())
            .currentStock(initialStock)
            .minStockThreshold(minThreshold)
            .unitCost(unitCost)
            .createdAt(Instant.now())
            .build();

        Material saved = materialRepository.save(material);

        if (initialStock.compareTo(BigDecimal.ZERO) > 0) {
            StockMovement initialMovement = StockMovement.builder()
                .material(saved)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(initialStock)
                .balanceAfter(initialStock)
                .notes("Initial stock entry")
                .performedBy(principal.getId())
                .createdAt(Instant.now())
                .build();
            stockMovementRepository.save(initialMovement);
        }

        return MaterialDto.fromEntity(saved);
    }

    @Transactional
    public MaterialDto adjustStock(String projectId, String materialId, StockAdjustDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));

        if (!material.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Material does not belong to project " + projectId);
        }

        BigDecimal qty = dto.quantity().abs();
        BigDecimal currentStock = material.getCurrentStock();
        BigDecimal newStock;

        boolean isDeduction = (dto.movementType() == MovementType.OUTWARD_ISSUE)
            || (dto.movementType() == MovementType.ADJUSTMENT && dto.quantity().compareTo(BigDecimal.ZERO) < 0);

        if (isDeduction) {
            if (currentStock.compareTo(qty) < 0) {
                throw new BadRequestException("Insufficient stock. Available: " + currentStock + ", Requested deduction: " + qty);
            }
            newStock = currentStock.subtract(qty);
        } else {
            newStock = currentStock.add(qty);
        }

        material.setCurrentStock(newStock);
        Material saved = materialRepository.save(material);

        StockMovement movement = StockMovement.builder()
            .material(saved)
            .movementType(dto.movementType())
            .quantity(isDeduction ? qty.negate() : qty)
            .balanceAfter(newStock)
            .referenceId(dto.referenceId())
            .notes(dto.notes())
            .performedBy(principal.getId())
            .createdAt(Instant.now())
            .build();
        stockMovementRepository.save(movement);

        return MaterialDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MaterialRequestDto> getMaterialRequests(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return materialRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(MaterialRequestDto::fromEntity)
            .toList();
    }

    @Transactional
    public MaterialRequestDto createMaterialRequest(String projectId, MaterialRequestCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        Material material = materialRepository.findById(dto.materialId())
            .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + dto.materialId()));

        String reqId = "mr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        MaterialRequest request = MaterialRequest.builder()
            .id(reqId)
            .project(project)
            .material(material)
            .quantity(dto.quantity())
            .requiredByDate(dto.requiredByDate())
            .requestedBy(principal.getId())
            .status(MaterialRequestStatus.PENDING)
            .phaseOrTaskDesc(dto.phaseOrTaskDesc())
            .createdAt(Instant.now())
            .build();

        MaterialRequest saved = materialRequestRepository.save(request);
        return MaterialRequestDto.fromEntity(saved);
    }

    @Transactional
    public MaterialRequestDto approveMaterialRequest(String projectId, String requestId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        MaterialRequest request = materialRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Material request not found: " + requestId));

        if (!request.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Material request does not belong to project " + projectId);
        }

        if (principal != null && request.getRequestedBy() != null && request.getRequestedBy().equals(principal.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Self-approval not permitted. Submitter cannot approve their own material request.");
        }

        request.setStatus(MaterialRequestStatus.APPROVED);
        request.setApprovedBy(principal != null ? principal.getId() : "system");

        MaterialRequest saved = materialRequestRepository.save(request);
        return MaterialRequestDto.fromEntity(saved);
    }

    @Transactional
    public MaterialRequestDto rejectMaterialRequest(String projectId, String requestId, MaterialRequestRejectDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        MaterialRequest request = materialRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Material request not found: " + requestId));

        if (!request.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Material request does not belong to project " + projectId);
        }

        request.setStatus(MaterialRequestStatus.REJECTED);
        request.setApprovedBy(principal != null ? principal.getId() : "system");
        request.setRejectionReason(dto.reason());

        MaterialRequest saved = materialRequestRepository.save(request);
        return MaterialRequestDto.fromEntity(saved);
    }

    @Transactional
    public MaterialRequestDto issueMaterialRequest(String projectId, String requestId, MaterialRequestIssueDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        MaterialRequest request = materialRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Material request not found: " + requestId));

        if (!request.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Material request does not belong to project " + projectId);
        }

        if (request.getStatus() != MaterialRequestStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED material requests can be issued. Current status: " + request.getStatus());
        }

        Material material = request.getMaterial();
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal requestedQty = request.getQuantity();

        if (currentStock.compareTo(requestedQty) < 0) {
            throw new BadRequestException("Insufficient stock to issue request. Available: " + currentStock + " " + material.getUnit() + ", Required: " + requestedQty);
        }

        BigDecimal newStock = currentStock.subtract(requestedQty);
        material.setCurrentStock(newStock);
        materialRepository.save(material);

        StockMovement movement = StockMovement.builder()
            .material(material)
            .movementType(MovementType.OUTWARD_ISSUE)
            .quantity(requestedQty.negate())
            .balanceAfter(newStock)
            .referenceId(requestId)
            .notes(dto != null && dto.notes() != null ? dto.notes() : "Issued for MRN: " + requestId + " (" + request.getPhaseOrTaskDesc() + ")")
            .idempotencyKey(dto != null ? dto.idempotencyKey() : null)
            .performedBy(principal != null ? principal.getId() : "system")
            .createdAt(Instant.now())
            .build();
        stockMovementRepository.save(movement);

        request.setStatus(MaterialRequestStatus.ISSUED);
        request.setIssuedBy(principal != null ? principal.getId() : "system");
        request.setIssuedAt(Instant.now());

        MaterialRequest saved = materialRequestRepository.save(request);
        return MaterialRequestDto.fromEntity(saved);
    }

    @Transactional
    public MaterialDto returnStock(String projectId, String materialId, StockReturnDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));

        if (!material.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Material does not belong to project " + projectId);
        }

        BigDecimal returnQty = dto.quantity().abs();
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal newStock = currentStock.add(returnQty);

        material.setCurrentStock(newStock);
        Material saved = materialRepository.save(material);

        StockMovement movement = StockMovement.builder()
            .material(saved)
            .movementType(MovementType.RETURN)
            .quantity(returnQty)
            .balanceAfter(newStock)
            .referenceId(dto.referenceId())
            .notes(dto.notes() != null ? dto.notes() : "Unused material returned to store")
            .performedBy(principal != null ? principal.getId() : "system")
            .createdAt(Instant.now())
            .build();
        stockMovementRepository.save(movement);

        return MaterialDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> getLowStockMaterials(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return materialRepository.findLowStockMaterials(projectId).stream()
            .map(MaterialDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> getProjectStockLedger(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return stockMovementRepository.findByMaterialProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(StockMovementDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> getStockMovements(String projectId, String materialId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return stockMovementRepository.findByMaterialIdOrderByCreatedAtDesc(materialId).stream()
            .map(StockMovementDto::fromEntity)
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
