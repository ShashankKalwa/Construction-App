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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcurementService {

    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final GoodsReceiptNoteRepository grnRepository;
    private final MaterialRepository materialRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;

    public ProcurementService(
        VendorRepository vendorRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        PurchaseOrderItemRepository purchaseOrderItemRepository,
        GoodsReceiptNoteRepository grnRepository,
        MaterialRepository materialRepository,
        StockMovementRepository stockMovementRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        OrganizationRepository organizationRepository
    ) {
        this.vendorRepository = vendorRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.grnRepository = grnRepository;
        this.materialRepository = materialRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<VendorDto> getVendors(String organizationId) {
        return vendorRepository.findByOrganizationIdOrderByNameAsc(organizationId).stream()
            .map(VendorDto::fromEntity)
            .toList();
    }

    @Transactional
    public VendorDto createVendor(String organizationId, VendorCreateDto dto) {
        Organization org = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));

        String vendorId = "ven_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Vendor vendor = Vendor.builder()
            .id(vendorId)
            .organization(org)
            .name(dto.name())
            .gstin(dto.gstin())
            .phone(dto.phone())
            .email(dto.email())
            .category(dto.category())
            .paymentTerms(dto.paymentTerms())
            .build();

        Vendor saved = vendorRepository.save(vendor);
        return VendorDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderDto> getPurchaseOrders(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return purchaseOrderRepository.findByProjectIdOrderByOrderDateDesc(projectId).stream()
            .map(this::mapToPoDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDto getPurchaseOrder(String projectId, String poId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + poId));
        return mapToPoDto(po);
    }

    @Transactional
    public PurchaseOrderDto createPurchaseOrder(String projectId, PurchaseOrderCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        Vendor vendor = vendorRepository.findById(dto.getVendorId())
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + dto.getVendorId()));

        String poNumber = dto.getPoNumber();
        if (poNumber == null || poNumber.isBlank()) {
            poNumber = "PO-" + System.currentTimeMillis() % 1000000;
        }

        String poId = "po_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        PurchaseOrder po = PurchaseOrder.builder()
            .id(poId)
            .poNumber(poNumber)
            .project(project)
            .vendor(vendor)
            .orderDate(dto.getOrderDate())
            .deliveryDeadline(dto.getDeliveryDeadline())
            .totalAmount(BigDecimal.ZERO)
            .status(PurchaseOrderStatus.ISSUED)
            .createdBy(principal.getId())
            .items(new ArrayList<>())
            .createdAt(Instant.now())
            .build();

        BigDecimal runningTotal = BigDecimal.ZERO;

        for (PurchaseOrderItemCreateDto itemDto : dto.getItems()) {
            Material material = materialRepository.findById(itemDto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + itemDto.getMaterialId()));

            BigDecimal totalItemPrice = itemDto.getOrderedQuantity().multiply(itemDto.getUnitPrice());
            runningTotal = runningTotal.add(totalItemPrice);

            String itemId = "poi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                .id(itemId)
                .purchaseOrder(po)
                .material(material)
                .orderedQuantity(itemDto.getOrderedQuantity())
                .unitPrice(itemDto.getUnitPrice())
                .totalPrice(totalItemPrice)
                .receivedQuantity(BigDecimal.ZERO)
                .build();

            po.getItems().add(item);
        }

        po.setTotalAmount(runningTotal);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPoDto(saved);
    }

    @Transactional
    public PurchaseOrderDto updatePoStatus(String projectId, String poId, PurchaseOrderStatus status, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + poId));

        if (!po.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Purchase order does not belong to project " + projectId);
        }

        po.setStatus(status);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return mapToPoDto(saved);
    }

    @Transactional
    public GoodsReceiptNoteDto recordGrn(String projectId, GrnCreateDto dto, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        PurchaseOrder po = purchaseOrderRepository.findById(dto.getPurchaseOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + dto.getPurchaseOrderId()));

        if (!po.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Purchase order does not belong to project " + projectId);
        }

        Material material = materialRepository.findById(dto.getMaterialId())
            .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + dto.getMaterialId()));

        PurchaseOrderItem matchingItem = po.getItems().stream()
            .filter(item -> item.getMaterial().getId().equals(material.getId()))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Material " + material.getName() + " is not an item in PO " + po.getPoNumber()));

        BigDecimal currentReceived = matchingItem.getReceivedQuantity() != null ? matchingItem.getReceivedQuantity() : BigDecimal.ZERO;
        matchingItem.setReceivedQuantity(currentReceived.add(dto.getReceivedQuantity()));
        purchaseOrderItemRepository.save(matchingItem);

        String grnId = "grn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        GoodsReceiptNote grn = GoodsReceiptNote.builder()
            .id(grnId)
            .purchaseOrder(po)
            .material(material)
            .challanNumber(dto.getChallanNumber())
            .receivedQuantity(dto.getReceivedQuantity())
            .receivedDate(dto.getReceivedDate())
            .inspectedBy(principal.getId())
            .qualityRemarks(dto.getQualityRemarks())
            .createdAt(Instant.now())
            .build();
        GoodsReceiptNote savedGrn = grnRepository.save(grn);

        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal newStock = currentStock.add(dto.getReceivedQuantity());
        material.setCurrentStock(newStock);
        materialRepository.save(material);

        StockMovement movement = StockMovement.builder()
            .material(material)
            .movementType(MovementType.INWARD_GRN)
            .quantity(dto.getReceivedQuantity())
            .balanceAfter(newStock)
            .referenceId(dto.getChallanNumber())
            .notes("GRN: " + grnId + " for PO: " + po.getPoNumber())
            .performedBy(principal.getId())
            .createdAt(Instant.now())
            .build();
        stockMovementRepository.save(movement);

        boolean allFulfilled = true;
        boolean partiallyFulfilled = false;
        for (PurchaseOrderItem item : po.getItems()) {
            if (item.getReceivedQuantity().compareTo(item.getOrderedQuantity()) < 0) {
                allFulfilled = false;
            }
            if (item.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                partiallyFulfilled = true;
            }
        }

        if (allFulfilled) {
            po.setStatus(PurchaseOrderStatus.COMPLETED);
        } else if (partiallyFulfilled) {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_DELIVERED);
        }
        purchaseOrderRepository.save(po);

        return GoodsReceiptNoteDto.builder()
            .id(savedGrn.getId())
            .purchaseOrderId(po.getId())
            .poNumber(po.getPoNumber())
            .materialId(material.getId())
            .materialName(material.getName())
            .unit(material.getUnit())
            .challanNumber(savedGrn.getChallanNumber())
            .receivedQuantity(savedGrn.getReceivedQuantity())
            .receivedDate(savedGrn.getReceivedDate())
            .inspectedBy(savedGrn.getInspectedBy())
            .qualityRemarks(savedGrn.getQualityRemarks())
            .createdAt(savedGrn.getCreatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public List<GoodsReceiptNoteDto> getGrnsForPo(String projectId, String poId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return grnRepository.findByPurchaseOrderIdOrderByReceivedDateDesc(poId).stream()
            .map(grn -> GoodsReceiptNoteDto.builder()
                .id(grn.getId())
                .purchaseOrderId(grn.getPurchaseOrder().getId())
                .poNumber(grn.getPurchaseOrder().getPoNumber())
                .materialId(grn.getMaterial().getId())
                .materialName(grn.getMaterial().getName())
                .unit(grn.getMaterial().getUnit())
                .challanNumber(grn.getChallanNumber())
                .receivedQuantity(grn.getReceivedQuantity())
                .receivedDate(grn.getReceivedDate())
                .inspectedBy(grn.getInspectedBy())
                .qualityRemarks(grn.getQualityRemarks())
                .createdAt(grn.getCreatedAt())
                .build())
            .toList();
    }

    private PurchaseOrderDto mapToPoDto(PurchaseOrder po) {
        List<PurchaseOrderItemDto> itemDtos = po.getItems().stream()
            .map(i -> PurchaseOrderItemDto.builder()
                .id(i.getId())
                .purchaseOrderId(po.getId())
                .materialId(i.getMaterial().getId())
                .materialName(i.getMaterial().getName())
                .unit(i.getMaterial().getUnit())
                .orderedQuantity(i.getOrderedQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .receivedQuantity(i.getReceivedQuantity())
                .build())
            .toList();

        return PurchaseOrderDto.builder()
            .id(po.getId())
            .poNumber(po.getPoNumber())
            .projectId(po.getProject().getId())
            .vendorId(po.getVendor().getId())
            .vendorName(po.getVendor().getName())
            .orderDate(po.getOrderDate())
            .deliveryDeadline(po.getDeliveryDeadline())
            .totalAmount(po.getTotalAmount())
            .status(po.getStatus())
            .createdBy(po.getCreatedBy())
            .items(itemDtos)
            .createdAt(po.getCreatedAt())
            .updatedAt(po.getUpdatedAt())
            .build();
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
