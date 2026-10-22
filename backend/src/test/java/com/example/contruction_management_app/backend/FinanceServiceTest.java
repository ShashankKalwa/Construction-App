package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.repository.ContractorBillRepository;
import com.example.contruction_management_app.backend.repository.ExpenseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.FinanceService;
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

public class FinanceServiceTest {

    private ExpenseRepository expenseRepository;
    private ContractorBillRepository contractorBillRepository;
    private ProjectRepository projectRepository;
    private ProjectMemberRepository projectMemberRepository;
    private FinanceService financeService;

    private Project mockProject;
    private Organization mockOrg;
    private UserPrincipal financePrincipal;

    @BeforeEach
    void setUp() {
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        contractorBillRepository = Mockito.mock(ContractorBillRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);

        financeService = new FinanceService(
            expenseRepository,
            contractorBillRepository,
            projectRepository,
            projectMemberRepository
        );

        mockOrg = new Organization("Green Valley", "green-valley");
        mockOrg.setId("org_green_valley");

        mockProject = new Project("Green Valley Residency", "GVR-01", "Bengaluru", new BigDecimal("50000000"), mockOrg);
        mockProject.setId("prj_green_valley_residency");

        financePrincipal = new UserPrincipal(
            "usr_accountant", "accountant@greenvalley.com", "hash",
            "org_green_valley", "accountant", List.of(), true
        );

        when(projectRepository.findById("prj_green_valley_residency")).thenReturn(Optional.of(mockProject));
        when(projectMemberRepository.existsByProjectIdAndUserId(any(), any())).thenReturn(true);
    }

    @Test
    void testCreateContractorBill_CalculatesRetentionAndTds_Correctly() {
        when(contractorBillRepository.save(any(ContractorBill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Gross: 100,000 | Retention 5% = 5,000 | TDS 2% = 2,000 | Net Payable = 93,000
        ContractorBillCreateDto dto = ContractorBillCreateDto.builder()
            .contractorName("Apex Shuttering Works")
            .billNumber("RA-04")
            .billDate(LocalDate.now())
            .grossAmount(BigDecimal.valueOf(100000.00))
            .retentionPct(BigDecimal.valueOf(5.00))
            .tdsPct(BigDecimal.valueOf(2.00))
            .workDescription("Formwork for 3rd floor slab")
            .build();

        ContractorBillDto bill = financeService.createContractorBill("prj_green_valley_residency", dto, financePrincipal);

        assertNotNull(bill);
        assertEquals(0, BigDecimal.valueOf(100000.00).compareTo(bill.getGrossAmount()));
        assertEquals(0, BigDecimal.valueOf(5000.00).compareTo(bill.getRetentionAmount()));
        assertEquals(0, BigDecimal.valueOf(2000.00).compareTo(bill.getTdsAmount()));
        assertEquals(0, BigDecimal.valueOf(93000.00).compareTo(bill.getNetPayable()));
        assertEquals(BillPaymentStatus.PENDING, bill.getStatus());
        assertEquals("usr_accountant", bill.getCertifiedBy());
    }

    @Test
    void testCreateExpense_Success() {
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseCreateDto dto = ExpenseCreateDto.builder()
            .category(ExpenseCategory.EQUIPMENT)
            .title("Excavator Fuel 200L")
            .amount(BigDecimal.valueOf(19000.00))
            .expenseDate(LocalDate.now())
            .receiptUrl("https://example.com/receipt.pdf")
            .build();

        ExpenseDto exp = financeService.createExpense("prj_green_valley_residency", dto, financePrincipal);

        assertNotNull(exp);
        assertEquals(ExpenseCategory.EQUIPMENT, exp.getCategory());
        assertEquals(0, BigDecimal.valueOf(19000.00).compareTo(exp.getAmount()));
        assertEquals(ExpenseStatus.SUBMITTED, exp.getStatus());
        assertEquals("usr_accountant", exp.getSubmittedBy());
    }

    @Test
    void testExpenseApproval_StepperWorkflow() {
        Expense expense = Expense.builder()
            .id("exp_01")
            .project(mockProject)
            .category(ExpenseCategory.MATERIALS)
            .title("Safety Helmets and Jackets")
            .amount(BigDecimal.valueOf(15000.00))
            .expenseDate(LocalDate.now())
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy("usr_engineer")
            .build();

        when(expenseRepository.findById("exp_01")).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Step 1: VERIFIED
        ExpenseDto verified = financeService.updateExpenseStatus("prj_green_valley_residency", "exp_01", ExpenseStatus.VERIFIED, financePrincipal);
        assertEquals(ExpenseStatus.VERIFIED, verified.getStatus());
        assertEquals("usr_accountant", verified.getVerifiedBy());

        // Step 2: APPROVED
        ExpenseDto approved = financeService.updateExpenseStatus("prj_green_valley_residency", "exp_01", ExpenseStatus.APPROVED, financePrincipal);
        assertEquals(ExpenseStatus.APPROVED, approved.getStatus());
        assertEquals("usr_accountant", approved.getApprovedBy());

        // Step 3: PAID
        ExpenseDto paid = financeService.updateExpenseStatus("prj_green_valley_residency", "exp_01", ExpenseStatus.PAID, financePrincipal);
        assertEquals(ExpenseStatus.PAID, paid.getStatus());
        assertEquals("usr_accountant", paid.getPaidBy());
    }
}
