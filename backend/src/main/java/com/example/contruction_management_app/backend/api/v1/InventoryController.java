package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/projects/{projectId}/materials")
    public ResponseEntity<List<MaterialDto>> getMaterials(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.getMaterialsByProject(projectId, principal));
    }

    @GetMapping("/projects/{projectId}/materials/{materialId}")
    public ResponseEntity<MaterialDto> getMaterial(
        @PathVariable String projectId,
        @PathVariable String materialId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.getMaterial(projectId, materialId, principal));
    }

    @PostMapping("/projects/{projectId}/materials")
    public ResponseEntity<MaterialDto> createMaterial(
        @PathVariable String projectId,
        @Valid @RequestBody MaterialCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        MaterialDto created = inventoryService.createMaterial(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/projects/{projectId}/materials/{materialId}/adjust")
    public ResponseEntity<MaterialDto> adjustStock(
        @PathVariable String projectId,
        @PathVariable String materialId,
        @Valid @RequestBody StockAdjustDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.adjustStock(projectId, materialId, dto, principal));
    }

    @GetMapping("/projects/{projectId}/materials/{materialId}/movements")
    public ResponseEntity<List<StockMovementDto>> getStockMovements(
        @PathVariable String projectId,
        @PathVariable String materialId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.getStockMovements(projectId, materialId, principal));
    }

    @GetMapping("/projects/{projectId}/material-requests")
    public ResponseEntity<List<MaterialRequestDto>> getMaterialRequests(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.getMaterialRequests(projectId, principal));
    }

    @PostMapping("/projects/{projectId}/material-requests")
    public ResponseEntity<MaterialRequestDto> createMaterialRequest(
        @PathVariable String projectId,
        @Valid @RequestBody MaterialRequestCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        MaterialRequestDto created = inventoryService.createMaterialRequest(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/projects/{projectId}/material-requests/{requestId}/approve")
    public ResponseEntity<MaterialRequestDto> approveMaterialRequest(
        @PathVariable String projectId,
        @PathVariable String requestId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inventoryService.approveMaterialRequest(projectId, requestId, principal));
    }
}
