package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryWorkflowServiceTest {

    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private MaterialRequestRepository materialRequestRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;

    private InventoryService inventoryService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal supervisorUser;
    private UserPrincipal pmUser;
    private UserPrincipal storekeeperUser;
    private Material mockMaterial;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
            materialRepository,
            stockMovementRepository,
            materialRequestRepository,
            projectRepository,
            projectMemberRepository
        );

        mockOrg = new Organization("Apex Infra Ltd", "apex-infra");
        mockOrg.setId("org_apex_1");

        mockProject = new Project("Commercial Tower B", "CTB-02", "Sector 62, Gurgaon", new BigDecimal("15000000.00"), mockOrg);
        mockProject.setId("prj_commercial_tower_b");

        supervisorUser = new UserPrincipal(
            "usr_supervisor_amit",
            "amit@apexinfra.com",
            "hash",
            "org_apex_1",
            "site_supervisor",
            Collections.singletonList(new SimpleGrantedAuthority("materials:request"))
        );

        pmUser = new UserPrincipal(
            "usr_pm_rahul",
            "rahul@apexinfra.com",
            "hash",
            "org_apex_1",
            "project_manager",
            Collections.singletonList(new SimpleGrantedAuthority("materials:approve"))
        );

        storekeeperUser = new UserPrincipal(
            "usr_store_ramesh",
            "ramesh@apexinfra.com",
            "hash",
            "org_apex_1",
            "storekeeper",
            Collections.singletonList(new SimpleGrantedAuthority("materials:issue"))
        );

        mockMaterial = Material.builder()
            .id("mat_cem_1")
            .project(mockProject)
            .code("MAT-CEM-01")
            .name("OPC 53 Cement")
            .category(MaterialCategory.CEMENT)
            .unit("Bags")
            .currentStock(new BigDecimal("100.00"))
            .minStockThreshold(new BigDecimal("20.00"))
            .unitCost(new BigDecimal("380.00"))
            .createdAt(Instant.now())
            .build();

        lenient().when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(mockProject));
        lenient().when(projectMemberRepository.existsByProjectIdAndUserId(eq("prj_commercial_tower_b"), anyString())).thenReturn(true);
    }

    @Test
    void testMaterialRequestCreation() {
        when(materialRepository.findById("mat_cem_1")).thenReturn(Optional.of(mockMaterial));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequestCreateDto dto = new MaterialRequestCreateDto(
            "mat_cem_1",
            new BigDecimal("25.00"),
            LocalDate.now().plusDays(2),
            "Level 1 Slab Pour"
        );

        MaterialRequestDto result = inventoryService.createMaterialRequest("prj_commercial_tower_b", dto, supervisorUser);

        assertNotNull(result);
        assertEquals(new BigDecimal("25.00"), result.quantity());
        assertEquals(MaterialRequestStatus.PENDING, result.status());
        assertEquals("usr_supervisor_amit", result.requestedBy());
    }

    @Test
    void testDualControlPreventsSubmitterFromSelfApprovingMaterialRequest() {
        MaterialRequest request = MaterialRequest.builder()
            .id("mr_1")
            .project(mockProject)
            .material(mockMaterial)
            .quantity(new BigDecimal("25.00"))
            .requestedBy("usr_supervisor_amit")
            .status(MaterialRequestStatus.PENDING)
            .build();

        when(materialRequestRepository.findById("mr_1")).thenReturn(Optional.of(request));

        // Submitter attempts to approve their own request -> MUST THROW AccessDeniedException
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
            inventoryService.approveMaterialRequest("prj_commercial_tower_b", "mr_1", supervisorUser)
        );
    }

    @Test
    void testPmApprovesMaterialRequestSuccessfully() {
        MaterialRequest request = MaterialRequest.builder()
            .id("mr_1")
            .project(mockProject)
            .material(mockMaterial)
            .quantity(new BigDecimal("25.00"))
            .requestedBy("usr_supervisor_amit")
            .status(MaterialRequestStatus.PENDING)
            .build();

        when(materialRequestRepository.findById("mr_1")).thenReturn(Optional.of(request));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequestDto approved = inventoryService.approveMaterialRequest("prj_commercial_tower_b", "mr_1", pmUser);

        assertNotNull(approved);
        assertEquals(MaterialRequestStatus.APPROVED, approved.status());
        assertEquals("usr_pm_rahul", approved.approvedBy());
    }

    @Test
    void testRejectMaterialRequest() {
        MaterialRequest request = MaterialRequest.builder()
            .id("mr_1")
            .project(mockProject)
            .material(mockMaterial)
            .quantity(new BigDecimal("25.00"))
            .requestedBy("usr_supervisor_amit")
            .status(MaterialRequestStatus.PENDING)
            .build();

        when(materialRequestRepository.findById("mr_1")).thenReturn(Optional.of(request));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequestRejectDto rejectDto = new MaterialRequestRejectDto("Excessive quantity requested for this pour phase");
        MaterialRequestDto rejected = inventoryService.rejectMaterialRequest("prj_commercial_tower_b", "mr_1", rejectDto, pmUser);

        assertNotNull(rejected);
        assertEquals(MaterialRequestStatus.REJECTED, rejected.status());
        assertEquals("Excessive quantity requested for this pour phase", rejected.rejectionReason());
    }

    @Test
    void testStorekeeperIssuesStockAgainstApprovedRequest() {
        MaterialRequest request = MaterialRequest.builder()
            .id("mr_1")
            .project(mockProject)
            .material(mockMaterial)
            .quantity(new BigDecimal("40.00"))
            .requestedBy("usr_supervisor_amit")
            .approvedBy("usr_pm_rahul")
            .status(MaterialRequestStatus.APPROVED)
            .build();

        when(materialRequestRepository.findById("mr_1")).thenReturn(Optional.of(request));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequestIssueDto issueDto = new MaterialRequestIssueDto("Dispensed 40 bags from Bay 2", "idemp_issue_1");
        MaterialRequestDto issued = inventoryService.issueMaterialRequest("prj_commercial_tower_b", "mr_1", issueDto, storekeeperUser);

        assertNotNull(issued);
        assertEquals(MaterialRequestStatus.ISSUED, issued.status());
        assertEquals("usr_store_ramesh", issued.issuedBy());
        assertNotNull(issued.issuedAt());

        // Verify material stock deducted: 100 - 40 = 60
        assertEquals(new BigDecimal("60.00"), mockMaterial.getCurrentStock());

        // Verify stock movement OUTWARD_ISSUE recorded
        verify(stockMovementRepository).save(argThat(sm ->
            sm.getMovementType() == MovementType.OUTWARD_ISSUE &&
            sm.getQuantity().compareTo(new BigDecimal("-40.00")) == 0 &&
            sm.getBalanceAfter().compareTo(new BigDecimal("60.00")) == 0
        ));
    }

    @Test
    void testIssueStockThrowsWhenInsufficientInventory() {
        MaterialRequest request = MaterialRequest.builder()
            .id("mr_1")
            .project(mockProject)
            .material(mockMaterial) // 100 bags available
            .quantity(new BigDecimal("150.00")) // Requests 150 bags
            .requestedBy("usr_supervisor_amit")
            .status(MaterialRequestStatus.APPROVED)
            .build();

        when(materialRequestRepository.findById("mr_1")).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class, () ->
            inventoryService.issueMaterialRequest("prj_commercial_tower_b", "mr_1", null, storekeeperUser)
        );
    }

    @Test
    void testReturnStockCreditsMaterialBalance() {
        when(materialRepository.findById("mat_cem_1")).thenReturn(Optional.of(mockMaterial));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        StockReturnDto returnDto = new StockReturnDto(
            new BigDecimal("10.00"),
            "mr_1",
            "10 unopened bags returned from Ground Floor slab pour"
        );

        MaterialDto result = inventoryService.returnStock("prj_commercial_tower_b", "mat_cem_1", returnDto, storekeeperUser);

        assertNotNull(result);
        // 100 + 10 = 110
        assertEquals(new BigDecimal("110.00"), mockMaterial.getCurrentStock());

        verify(stockMovementRepository).save(argThat(sm ->
            sm.getMovementType() == MovementType.RETURN &&
            sm.getQuantity().compareTo(new BigDecimal("10.00")) == 0 &&
            sm.getBalanceAfter().compareTo(new BigDecimal("110.00")) == 0
        ));
    }

    @Test
    void testLowStockDetection() {
        Material lowStockSteel = Material.builder()
            .id("mat_steel_12")
            .project(mockProject)
            .code("MAT-STL-12")
            .name("12mm TMT Rebar")
            .category(MaterialCategory.STEEL)
            .unit("MT")
            .currentStock(new BigDecimal("3.00"))
            .minStockThreshold(new BigDecimal("6.00")) // currentStock <= minStockThreshold
            .unitCost(new BigDecimal("65000.00"))
            .createdAt(Instant.now())
            .build();

        when(materialRepository.findLowStockMaterials("prj_commercial_tower_b"))
            .thenReturn(List.of(lowStockSteel));

        List<MaterialDto> lowStockList = inventoryService.getLowStockMaterials("prj_commercial_tower_b", pmUser);

        assertEquals(1, lowStockList.size());
        assertEquals("MAT-STL-12", lowStockList.get(0).code());
        assertTrue(lowStockList.get(0).isLowStock());
    }
}
