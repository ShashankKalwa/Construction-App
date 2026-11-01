package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.ProcurementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementWorkflowServiceTest {

    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock
    private GoodsReceiptNoteRepository grnRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private OrganizationRepository organizationRepository;

    private ProcurementService procurementService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal pmUser;
    private UserPrincipal storekeeperUser;
    private Vendor mockVendor;
    private Material mockCement;

    @BeforeEach
    void setUp() {
        procurementService = new ProcurementService(
            vendorRepository,
            purchaseOrderRepository,
            purchaseOrderItemRepository,
            grnRepository,
            materialRepository,
            stockMovementRepository,
            projectRepository,
            projectMemberRepository,
            organizationRepository
        );

        mockOrg = new Organization("Chaitanya InfraTech", "apex-infra");
        mockOrg.setId("org_apex_1");

        mockProject = new Project("Commercial Tower B", "CTB-02", "Sector 62, Gurgaon", new BigDecimal("50000000.00"), mockOrg);
        mockProject.setId("prj_commercial_tower_b");

        pmUser = new UserPrincipal(
            "usr_pm_rahul",
            "rahul@chaitanyainfratech.com",
            "hash",
            "org_apex_1",
            "project_manager",
            Collections.singletonList(new SimpleGrantedAuthority("po:create"))
        );

        storekeeperUser = new UserPrincipal(
            "usr_store_ramesh",
            "ramesh@chaitanyainfratech.com",
            "hash",
            "org_apex_1",
            "storekeeper",
            Collections.singletonList(new SimpleGrantedAuthority("grn:create"))
        );

        mockVendor = Vendor.builder()
            .id("vnd_ultratech")
            .organization(mockOrg)
            .name("UltraTech Cement Distribution Hub")
            .gstin("29AABCU1234F1Z1")
            .category("CEMENT_SUPPLIER")
            .paymentTerms("Net 30 Days")
            .build();

        mockCement = Material.builder()
            .id("mat_cem_opc")
            .project(mockProject)
            .code("MAT-CEM-01")
            .name("OPC 53 Grade Cement")
            .category(MaterialCategory.CEMENT)
            .unit("Bags")
            .currentStock(new BigDecimal("100.00"))
            .minStockThreshold(new BigDecimal("50.00"))
            .unitCost(new BigDecimal("380.00"))
            .build();

        lenient().when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(mockProject));
        lenient().when(projectMemberRepository.existsByProjectIdAndUserId(anyString(), anyString())).thenReturn(true);
        lenient().when(organizationRepository.findById("org_apex_1")).thenReturn(Optional.of(mockOrg));
    }

    @Test
    void testVendorCreation() {
        VendorCreateDto vendorDto = new VendorCreateDto(
            "Reliable Steels & TMT Depot",
            "29AAACT9876E1Z5",
            "+91 98450 11223",
            "sales@reliablesteels.com",
            "STEEL_TRADER",
            "Net 15 Days"
        );

        when(vendorRepository.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        VendorDto created = procurementService.createVendor("org_apex_1", vendorDto);
        assertNotNull(created);
        assertEquals("Reliable Steels & TMT Depot", created.name());
        assertEquals("29AAACT9876E1Z5", created.gstin());
    }

    @Test
    void testPurchaseOrderCreationWithLineItems() {
        when(vendorRepository.findById("vnd_ultratech")).thenReturn(Optional.of(mockVendor));
        when(materialRepository.findById("mat_cem_opc")).thenReturn(Optional.of(mockCement));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderItemCreateDto itemDto = PurchaseOrderItemCreateDto.builder()
            .materialId("mat_cem_opc")
            .orderedQuantity(new BigDecimal("500.00"))
            .unitPrice(new BigDecimal("380.00"))
            .build();

        PurchaseOrderCreateDto poDto = PurchaseOrderCreateDto.builder()
            .poNumber("PO-2026-0501")
            .vendorId("vnd_ultratech")
            .orderDate(LocalDate.now())
            .deliveryDeadline(LocalDate.now().plusDays(7))
            .items(List.of(itemDto))
            .build();

        PurchaseOrderDto created = procurementService.createPurchaseOrder("prj_commercial_tower_b", poDto, pmUser);
        assertNotNull(created);
        assertEquals("PO-2026-0501", created.getPoNumber());
        assertEquals(PurchaseOrderStatus.ISSUED, created.getStatus());
        // Total amount = 500 * 380 = 190,000.00
        assertEquals(new BigDecimal("190000.00"), created.getTotalAmount());
        assertEquals(1, created.getItems().size());
        assertEquals(new BigDecimal("500.00"), created.getItems().get(0).getOrderedQuantity());
    }

    @Test
    void testRecordGrn_InwardArrivalUpdatesStockAndLedgerAndPoStatus() {
        // Setup existing PO with 500 ordered bags
        PurchaseOrder po = PurchaseOrder.builder()
            .id("po_test_01")
            .poNumber("PO-2026-0501")
            .project(mockProject)
            .vendor(mockVendor)
            .orderDate(LocalDate.now())
            .status(PurchaseOrderStatus.ISSUED)
            .createdBy("usr_pm_rahul")
            .build();

        PurchaseOrderItem poItem = PurchaseOrderItem.builder()
            .id("poi_test_01")
            .purchaseOrder(po)
            .material(mockCement)
            .orderedQuantity(new BigDecimal("500.00"))
            .unitPrice(new BigDecimal("380.00"))
            .totalPrice(new BigDecimal("190000.00"))
            .receivedQuantity(BigDecimal.ZERO)
            .build();

        po.getItems().add(poItem);

        when(purchaseOrderRepository.findById("po_test_01")).thenReturn(Optional.of(po));
        when(materialRepository.findById("mat_cem_opc")).thenReturn(Optional.of(mockCement));
        when(grnRepository.save(any(GoodsReceiptNote.class))).thenAnswer(i -> i.getArgument(0));

        // Initial cement stock is 100.00
        assertEquals(new BigDecimal("100.00"), mockCement.getCurrentStock());

        // Storekeeper records delivery receipt (GRN) of 500 bags
        GrnCreateDto grnDto = GrnCreateDto.builder()
            .purchaseOrderId("po_test_01")
            .materialId("mat_cem_opc")
            .challanNumber("CHALLAN-99221")
            .receivedQuantity(new BigDecimal("500.00"))
            .receivedDate(LocalDate.now())
            .qualityRemarks("500 bags arrived intact, batch tested OK")
            .build();

        GoodsReceiptNoteDto grnResult = procurementService.recordGrn("prj_commercial_tower_b", grnDto, storekeeperUser);
        assertNotNull(grnResult);
        assertEquals("CHALLAN-99221", grnResult.getChallanNumber());
        assertEquals(new BigDecimal("500.00"), grnResult.getReceivedQuantity());

        // 1. PO item received quantity updated to 500
        assertEquals(new BigDecimal("500.00"), poItem.getReceivedQuantity());

        // 2. PO status transitioned to COMPLETED
        assertEquals(PurchaseOrderStatus.COMPLETED, po.getStatus());

        // 3. Material physical stock increased from 100 to 600
        assertEquals(new BigDecimal("600.00"), mockCement.getCurrentStock());

        // 4. Inward GRN stock movement recorded
        verify(stockMovementRepository, times(1)).save(argThat(sm ->
            sm.getMovementType() == MovementType.INWARD_GRN &&
            sm.getQuantity().compareTo(new BigDecimal("500.00")) == 0 &&
            sm.getBalanceAfter().compareTo(new BigDecimal("600.00")) == 0
        ));
    }
}

