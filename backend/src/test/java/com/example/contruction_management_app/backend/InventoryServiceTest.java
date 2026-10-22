package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class InventoryServiceTest {

    private MaterialRepository materialRepository;
    private StockMovementRepository stockMovementRepository;
    private MaterialRequestRepository materialRequestRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private InventoryService inventoryService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal storeManagerPrincipal;

    @BeforeEach
    void setUp() {
        materialRepository = Mockito.mock(MaterialRepository.class);
        stockMovementRepository = Mockito.mock(StockMovementRepository.class);
        materialRequestRepository = Mockito.mock(MaterialRequestRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);

        inventoryService = new InventoryService(
            materialRepository,
            stockMovementRepository,
            materialRequestRepository,
            projectRepository,
            projectMemberRepository
        );

        mockOrg = new Organization("Green Valley", "green-valley");
        mockOrg.setId("org_green_valley");

        mockProject = new Project("Green Valley Residency", "GVR-01", "Bengaluru", new BigDecimal("50000000"), mockOrg);
        mockProject.setId("prj_green_valley_residency");

        storeManagerPrincipal = new UserPrincipal(
            "usr_store", "store@greenvalley.com", "hash",
            "org_green_valley", "store_manager", List.of(), true
        );

        when(projectRepository.findById("prj_green_valley_residency")).thenReturn(Optional.of(mockProject));
        when(projectMemberRepository.existsByProjectIdAndUserId(any(), any())).thenReturn(true);
    }

    @Test
    void testCreateMaterial_Success() {
        when(materialRepository.findByProjectIdAndCode("prj_green_valley_residency", "MAT-CEM-OPC"))
            .thenReturn(Optional.empty());
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaterialCreateDto dto = new MaterialCreateDto(
            "MAT-CEM-OPC", "OPC 53 Cement", MaterialCategory.CEMENT, "bags",
            BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(380.00)
        );

        MaterialDto result = inventoryService.createMaterial("prj_green_valley_residency", dto, storeManagerPrincipal);

        assertNotNull(result);
        assertEquals("MAT-CEM-OPC", result.code());
        assertEquals("OPC 53 Cement", result.name());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(result.currentStock()));
        assertFalse(result.isLowStock());
    }

    @Test
    void testAdjustStock_Deduct_Success() {
        Material material = Material.builder()
            .id("mat_01")
            .project(mockProject)
            .code("MAT-STEEL-12")
            .name("12mm TMT Fe550D")
            .category(MaterialCategory.STEEL)
            .unit("MT")
            .currentStock(BigDecimal.valueOf(15.00))
            .minStockThreshold(BigDecimal.valueOf(5.00))
            .unitCost(BigDecimal.valueOf(65000.00))
            .build();

        when(materialRepository.findById("mat_01")).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockAdjustDto adjustDto = new StockAdjustDto(
            MovementType.OUTWARD_ISSUE,
            BigDecimal.valueOf(5.00),
            "REQ-001",
            "Issued for column casting"
        );

        MaterialDto result = inventoryService.adjustStock("prj_green_valley_residency", "mat_01", adjustDto, storeManagerPrincipal);

        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(result.currentStock()));
        assertFalse(result.isLowStock());
    }

    @Test
    void testAdjustStock_InsufficientStock_ThrowsBadRequest() {
        Material material = Material.builder()
            .id("mat_01")
            .project(mockProject)
            .code("MAT-STEEL-12")
            .name("12mm TMT Fe550D")
            .category(MaterialCategory.STEEL)
            .unit("MT")
            .currentStock(BigDecimal.valueOf(2.00))
            .minStockThreshold(BigDecimal.valueOf(5.00))
            .unitCost(BigDecimal.valueOf(65000.00))
            .build();

        when(materialRepository.findById("mat_01")).thenReturn(Optional.of(material));

        StockAdjustDto adjustDto = new StockAdjustDto(
            MovementType.OUTWARD_ISSUE,
            BigDecimal.valueOf(5.00),
            "REQ-002",
            "Exceeds stock"
        );

        assertThrows(BadRequestException.class, () -> {
            inventoryService.adjustStock("prj_green_valley_residency", "mat_01", adjustDto, storeManagerPrincipal);
        });
    }

    @Test
    void testMaterialRequest_CreateAndApprove() {
        Material material = Material.builder()
            .id("mat_01")
            .project(mockProject)
            .code("MAT-SAND")
            .name("M-Sand")
            .category(MaterialCategory.AGGREGATE)
            .unit("ton")
            .currentStock(BigDecimal.valueOf(50.00))
            .minStockThreshold(BigDecimal.valueOf(10.00))
            .unitCost(BigDecimal.valueOf(1200.00))
            .build();

        when(materialRepository.findById("mat_01")).thenReturn(Optional.of(material));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaterialRequestCreateDto reqDto = new MaterialRequestCreateDto(
            "mat_01", BigDecimal.valueOf(20.00), LocalDate.now().plusDays(2), "Plastering phase"
        );

        MaterialRequestDto created = inventoryService.createMaterialRequest("prj_green_valley_residency", reqDto, storeManagerPrincipal);

        assertNotNull(created);
        assertEquals(MaterialRequestStatus.PENDING, created.status());
        assertEquals("usr_store", created.requestedBy());

        MaterialRequest entity = MaterialRequest.builder()
            .id(created.id())
            .project(mockProject)
            .material(material)
            .quantity(created.quantity())
            .status(MaterialRequestStatus.PENDING)
            .requestedBy("usr_store")
            .build();

        when(materialRequestRepository.findById(created.id())).thenReturn(Optional.of(entity));

        MaterialRequestDto approved = inventoryService.approveMaterialRequest("prj_green_valley_residency", created.id(), storeManagerPrincipal);

        assertNotNull(approved);
        assertEquals(MaterialRequestStatus.APPROVED, approved.status());
        assertEquals("usr_store", approved.approvedBy());
    }
}
