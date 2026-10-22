package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.PurchaseOrderStatus;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.ProcurementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @GetMapping("/organizations/{organizationId}/vendors")
    public ResponseEntity<List<VendorDto>> getVendors(@PathVariable String organizationId) {
        return ResponseEntity.ok(procurementService.getVendors(organizationId));
    }

    @PostMapping("/organizations/{organizationId}/vendors")
    public ResponseEntity<VendorDto> createVendor(
        @PathVariable String organizationId,
        @Valid @RequestBody VendorCreateDto dto
    ) {
        VendorDto created = procurementService.createVendor(organizationId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/projects/{projectId}/purchase-orders")
    public ResponseEntity<List<PurchaseOrderDto>> getPurchaseOrders(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(procurementService.getPurchaseOrders(projectId, principal));
    }

    @GetMapping("/projects/{projectId}/purchase-orders/{poId}")
    public ResponseEntity<PurchaseOrderDto> getPurchaseOrder(
        @PathVariable String projectId,
        @PathVariable String poId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(procurementService.getPurchaseOrder(projectId, poId, principal));
    }

    @PostMapping("/projects/{projectId}/purchase-orders")
    public ResponseEntity<PurchaseOrderDto> createPurchaseOrder(
        @PathVariable String projectId,
        @Valid @RequestBody PurchaseOrderCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PurchaseOrderDto created = procurementService.createPurchaseOrder(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/projects/{projectId}/purchase-orders/{poId}/status")
    public ResponseEntity<PurchaseOrderDto> updatePoStatus(
        @PathVariable String projectId,
        @PathVariable String poId,
        @RequestParam PurchaseOrderStatus status,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(procurementService.updatePoStatus(projectId, poId, status, principal));
    }

    @PostMapping("/projects/{projectId}/grn")
    public ResponseEntity<GoodsReceiptNoteDto> recordGrn(
        @PathVariable String projectId,
        @Valid @RequestBody GrnCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        GoodsReceiptNoteDto created = procurementService.recordGrn(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/projects/{projectId}/purchase-orders/{poId}/grn")
    public ResponseEntity<List<GoodsReceiptNoteDto>> getGrnsForPo(
        @PathVariable String projectId,
        @PathVariable String poId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(procurementService.getGrnsForPo(projectId, poId, principal));
    }
}
