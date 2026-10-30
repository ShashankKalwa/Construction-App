package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.FinanceService;
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
class FinanceAndCostDashboardTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ContractorBillRepository contractorBillRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private MaterialRepository materialRepository;

    private FinanceService financeService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal supervisorUser;
    private UserPrincipal pmUser;
    private UserPrincipal accountantUser;

    @BeforeEach
    void setUp() {
        financeService = new FinanceService(
            expenseRepository,
            contractorBillRepository,
            projectRepository,
            projectMemberRepository,
            purchaseOrderRepository,
            materialRepository
        );

        mockOrg = new Organization("Apex Infra Ltd", "apex-infra");
        mockOrg.setId("org_apex_1");

        mockProject = new Project("Commercial Tower B", "CTB-02", "Sector 62, Gurgaon", new BigDecimal("50000000.00"), mockOrg);
        mockProject.setId("prj_commercial_tower_b");

        supervisorUser = new UserPrincipal(
            "usr_supervisor_amit",
            "amit@apexinfra.com",
            "hash",
            "org_apex_1",
            "site_supervisor",
            Collections.singletonList(new SimpleGrantedAuthority("expenses:submit"))
        );

        pmUser = new UserPrincipal(
            "usr_pm_rahul",
            "rahul@apexinfra.com",
            "hash",
            "org_apex_1",
            "project_manager",
            Collections.singletonList(new SimpleGrantedAuthority("expenses:approve"))
        );

        accountantUser = new UserPrincipal(
            "usr_acc_neha",
            "neha@apexinfra.com",
            "hash",
            "org_apex_1",
            "accountant",
            Collections.singletonList(new SimpleGrantedAuthority("finance:view"))
        );

        lenient().when(projectRepository.findById("prj_commercial_tower_b")).thenReturn(Optional.of(mockProject));
        lenient().when(projectMemberRepository.existsByProjectIdAndUserId(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void testExpenseStepperWorkflow_SubmittedToVerifiedToApprovedToPaid() {
        // 1. Submit Expense
        ExpenseCreateDto createDto = ExpenseCreateDto.builder()
            .category(ExpenseCategory.EQUIPMENT)
            .title("JCB Excavator 3-Day Rental")
            .amount(new BigDecimal("45000.00"))
            .expenseDate(LocalDate.now())
            .receiptUrl("https://storage/receipt_001.pdf")
            .idempotencyKey("IDEM-EXP-001")
            .build();

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDto created = financeService.createExpense("prj_commercial_tower_b", createDto, supervisorUser);
        assertNotNull(created);
        assertEquals(ExpenseStatus.SUBMITTED, created.getStatus());
        assertEquals("usr_supervisor_amit", created.getSubmittedBy());
        assertEquals("IDEM-EXP-001", created.getIdempotencyKey());

        // 2. Verify Expense
        Expense expEntity = Expense.builder()
            .id(created.getId())
            .project(mockProject)
            .category(ExpenseCategory.EQUIPMENT)
            .title(created.getTitle())
            .amount(created.getAmount())
            .expenseDate(created.getExpenseDate())
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy("usr_supervisor_amit")
            .createdAt(Instant.now())
            .build();

        when(expenseRepository.findById(created.getId())).thenReturn(Optional.of(expEntity));

        ExpenseDto verified = financeService.updateExpenseStatus(
            "prj_commercial_tower_b",
            created.getId(),
            ExpenseStatus.VERIFIED,
            null,
            null,
            pmUser
        );
        assertEquals(ExpenseStatus.VERIFIED, verified.getStatus());
        assertEquals("usr_pm_rahul", verified.getVerifiedBy());

        // 3. Approve Expense (PM approves)
        ExpenseDto approved = financeService.updateExpenseStatus(
            "prj_commercial_tower_b",
            created.getId(),
            ExpenseStatus.APPROVED,
            null,
            null,
            pmUser
        );
        assertEquals(ExpenseStatus.APPROVED, approved.getStatus());
        assertEquals("usr_pm_rahul", approved.getApprovedBy());

        // 4. Pay Expense (Accountant pays with reference)
        ExpenseDto paid = financeService.updateExpenseStatus(
            "prj_commercial_tower_b",
            created.getId(),
            ExpenseStatus.PAID,
            null,
            "IMPS-TXN-998822",
            accountantUser
        );
        assertEquals(ExpenseStatus.PAID, paid.getStatus());
        assertEquals("usr_acc_neha", paid.getPaidBy());
        assertEquals("IMPS-TXN-998822", paid.getPaymentReference());
    }

    @Test
    void testExpenseDualControl_SubmitterCannotSelfApproveOrPay() {
        Expense expEntity = Expense.builder()
            .id("exp_test_01")
            .project(mockProject)
            .category(ExpenseCategory.MATERIALS)
            .title("Emergency Welding Rods")
            .amount(new BigDecimal("8500.00"))
            .expenseDate(LocalDate.now())
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy("usr_supervisor_amit")
            .createdAt(Instant.now())
            .build();

        when(expenseRepository.findById("exp_test_01")).thenReturn(Optional.of(expEntity));

        // Submitter attempting to self-approve must be rejected
        ForbiddenException ex1 = assertThrows(ForbiddenException.class, () ->
            financeService.updateExpenseStatus(
                "prj_commercial_tower_b",
                "exp_test_01",
                ExpenseStatus.APPROVED,
                null,
                null,
                supervisorUser
            )
        );
        assertTrue(ex1.getMessage().contains("Dual-control violation"));

        // Submitter attempting to self-pay must be rejected
        ForbiddenException ex2 = assertThrows(ForbiddenException.class, () ->
            financeService.updateExpenseStatus(
                "prj_commercial_tower_b",
                "exp_test_01",
                ExpenseStatus.PAID,
                null,
                "REF-001",
                supervisorUser
            )
        );
        assertTrue(ex2.getMessage().contains("Dual-control violation"));
    }

    @Test
    void testExpenseRejectionWithReason() {
        Expense expEntity = Expense.builder()
            .id("exp_test_02")
            .project(mockProject)
            .category(ExpenseCategory.PETTY_CASH)
            .title("Unapproved Taxi Fare")
            .amount(new BigDecimal("3200.00"))
            .expenseDate(LocalDate.now())
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy("usr_supervisor_amit")
            .createdAt(Instant.now())
            .build();

        when(expenseRepository.findById("exp_test_02")).thenReturn(Optional.of(expEntity));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));

        ExpenseDto rejected = financeService.updateExpenseStatus(
            "prj_commercial_tower_b",
            "exp_test_02",
            ExpenseStatus.REJECTED,
            "Outside allowable site travel budget policy",
            null,
            pmUser
        );
        assertEquals(ExpenseStatus.REJECTED, rejected.getStatus());
        assertEquals("Outside allowable site travel budget policy", rejected.getRejectionReason());
    }

    @Test
    void testContractorRaBillCreationWithRetentionAndTdsCalculation() {
        ContractorBillCreateDto billDto = ContractorBillCreateDto.builder()
            .contractorName("Apex Shuttering & Scaffolding Works")
            .billNumber("AFW-RA-01")
            .billDate(LocalDate.now())
            .grossAmount(new BigDecimal("500000.00"))
            .retentionPct(new BigDecimal("5.00"))
            .tdsPct(new BigDecimal("2.00"))
            .workDescription("Basement retaining wall falsework staging")
            .build();

        when(contractorBillRepository.save(any(ContractorBill.class))).thenAnswer(i -> i.getArgument(0));

        ContractorBillDto created = financeService.createContractorBill("prj_commercial_tower_b", billDto, pmUser);
        assertNotNull(created);
        assertEquals(new BigDecimal("500000.00"), created.getGrossAmount());
        // Retention: 5% of 500k = 25,000
        assertEquals(new BigDecimal("25000.00"), created.getRetentionAmount());
        // TDS: 2% of 500k = 10,000
        assertEquals(new BigDecimal("10000.00"), created.getTdsAmount());
        // Net Payable: 500k - 25k - 10k = 465,000
        assertEquals(new BigDecimal("465000.00"), created.getNetPayable());
        assertEquals(BillPaymentStatus.PENDING, created.getStatus());
    }

    @Test
    void testCostDashboardCalculation_ApprovedBudgetCommittedActualAndVariance() {
        // Setup Expenses: 1 PAID (120k), 1 APPROVED (72k)
        Expense paidExp = Expense.builder()
            .id("e1").project(mockProject).category(ExpenseCategory.EQUIPMENT)
            .amount(new BigDecimal("120000.00")).status(ExpenseStatus.PAID).build();
        Expense approvedExp = Expense.builder()
            .id("e2").project(mockProject).category(ExpenseCategory.UTILITIES)
            .amount(new BigDecimal("72000.00")).status(ExpenseStatus.APPROVED).build();
        when(expenseRepository.findByProjectIdOrderByExpenseDateDesc("prj_commercial_tower_b"))
            .thenReturn(List.of(paidExp, approvedExp));

        // Setup Contractor Bills: 1 APPROVED (Net 372k), 1 PENDING (Net 604.5k)
        ContractorBill approvedBill = ContractorBill.builder()
            .id("b1").project(mockProject).grossAmount(new BigDecimal("400000.00"))
            .netPayable(new BigDecimal("372000.00")).status(BillPaymentStatus.APPROVED).build();
        ContractorBill pendingBill = ContractorBill.builder()
            .id("b2").project(mockProject).grossAmount(new BigDecimal("650000.00"))
            .netPayable(new BigDecimal("604500.00")).status(BillPaymentStatus.PENDING).build();
        when(contractorBillRepository.findByProjectIdOrderByBillDateDesc("prj_commercial_tower_b"))
            .thenReturn(List.of(approvedBill, pendingBill));

        // Setup POs: 1 ISSUED (650k), 1 COMPLETED (190k)
        PurchaseOrder activePo = PurchaseOrder.builder()
            .id("po1").project(mockProject).totalAmount(new BigDecimal("650000.00"))
            .status(PurchaseOrderStatus.ISSUED).build();
        PurchaseOrder completedPo = PurchaseOrder.builder()
            .id("po2").project(mockProject).totalAmount(new BigDecimal("190000.00"))
            .status(PurchaseOrderStatus.COMPLETED).build();
        when(purchaseOrderRepository.findByProjectIdOrderByOrderDateDesc("prj_commercial_tower_b"))
            .thenReturn(List.of(activePo, completedPo));

        // Setup Materials for consumption
        Material mat = Material.builder()
            .id("m1").project(mockProject).name("OPC 53 Cement").unit("Bags")
            .currentStock(new BigDecimal("480.00")).unitCost(new BigDecimal("395.00")).build();
        when(materialRepository.findByProjectId("prj_commercial_tower_b")).thenReturn(List.of(mat));

        // Execute Dashboard
        CostDashboardSummaryDto dashboard = financeService.getCostDashboard("prj_commercial_tower_b", "TOTAL", pmUser);
        assertNotNull(dashboard);

        // Project baseline budget is 50,000,000.00
        assertEquals(new BigDecimal("50000000.00"), dashboard.getTotalBudget());

        // Actual Cost = Paid Expenses (120,000.00) + Paid Bills (0) = 120,000.00
        assertEquals(new BigDecimal("120000.00"), dashboard.getActualCost());

        // Committed Cost = Active PO (650,000.00) + Approved Bill (372,000.00) = 1,022,000.00
        assertEquals(new BigDecimal("1022000.00"), dashboard.getCommittedCost());

        // Remaining Budget = Total Budget - Actual Cost = 49,880,000.00
        assertEquals(new BigDecimal("49880000.00"), dashboard.getRemainingBudget());

        // Projected Final Cost = Actual Cost (120k) + Committed Cost (1022k) = 1,142,000.00
        assertEquals(new BigDecimal("1142000.00"), dashboard.getProjectedFinalCost());

        // Category breakdown and Top Materials
        assertFalse(dashboard.getCostBreakdown().isEmpty());
        assertFalse(dashboard.getTopMaterialConsumption().isEmpty());
    }
}

