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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class FinanceService {

    private final ExpenseRepository expenseRepository;
    private final ContractorBillRepository contractorBillRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRepository materialRepository;

    public FinanceService(
        ExpenseRepository expenseRepository,
        ContractorBillRepository contractorBillRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this(expenseRepository, contractorBillRepository, projectRepository, projectMemberRepository, null, null);
    }

    public FinanceService(
        ExpenseRepository expenseRepository,
        ContractorBillRepository contractorBillRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        MaterialRepository materialRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.contractorBillRepository = contractorBillRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.materialRepository = materialRepository;
    }

    // -------------------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpenses(String projectId, ExpenseCategory category, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        List<Expense> expenses;
        if (category != null) {
            expenses = expenseRepository.findByProjectIdAndCategoryOrderByExpenseDateDesc(projectId, category);
        } else {
            expenses = expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId);
        }
        return expenses.stream().map(this::mapToExpenseDto).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseDto getExpense(String projectId, String expenseId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        Expense exp = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));
        return mapToExpenseDto(exp);
    }

    @Transactional
    public ExpenseDto createExpense(String projectId, ExpenseCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String expenseId = "exp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Expense expense = Expense.builder()
            .id(expenseId)
            .project(project)
            .category(dto.getCategory())
            .title(dto.getTitle())
            .amount(dto.getAmount())
            .expenseDate(dto.getExpenseDate())
            .receiptUrl(dto.getReceiptUrl())
            .idempotencyKey(dto.getIdempotencyKey())
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        Expense saved = expenseRepository.save(expense);
        return mapToExpenseDto(saved);
    }

    @Transactional
    public ExpenseDto updateExpenseStatus(String projectId, String expenseId, ExpenseStatus status, UserPrincipal principal) {
        return updateExpenseStatus(projectId, expenseId, status, null, null, principal);
    }

    @Transactional
    public ExpenseDto updateExpenseStatus(
        String projectId,
        String expenseId,
        ExpenseStatus status,
        String rejectionReason,
        String paymentReference,
        UserPrincipal principal
    ) {
        validateProjectAccess(projectId, principal);

        Expense exp = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));

        if (!exp.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Expense does not belong to project " + projectId);
        }

        // Dual-control: A requester cannot approve or pay their own expense request
        if ((status == ExpenseStatus.APPROVED || status == ExpenseStatus.PAID) && principal.getId().equals(exp.getSubmittedBy())) {
            throw new ForbiddenException("Dual-control violation: Submitter cannot approve or pay their own expense request");
        }

        exp.setStatus(status);
        switch (status) {
            case VERIFIED -> exp.setVerifiedBy(principal.getId());
            case APPROVED -> exp.setApprovedBy(principal.getId());
            case PAID -> {
                exp.setPaidBy(principal.getId());
                if (paymentReference != null && !paymentReference.isBlank()) {
                    exp.setPaymentReference(paymentReference);
                }
            }
            case REJECTED -> {
                if (rejectionReason != null && !rejectionReason.isBlank()) {
                    exp.setRejectionReason(rejectionReason);
                }
            }
            default -> {}
        }

        Expense saved = expenseRepository.save(exp);
        return mapToExpenseDto(saved);
    }

    // -------------------------------------------------------------------------
    // Contractor Bills (RA Bills)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ContractorBillDto> getContractorBills(String projectId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        return contractorBillRepository.findByProjectIdOrderByBillDateDesc(projectId).stream()
            .map(this::mapToBillDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public ContractorBillDto getContractorBill(String projectId, String billId, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);
        ContractorBill bill = contractorBillRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Contractor bill not found: " + billId));
        return mapToBillDto(bill);
    }

    @Transactional
    public ContractorBillDto createContractorBill(String projectId, ContractorBillCreateDto dto, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        BigDecimal gross = dto.getGrossAmount();
        BigDecimal retentionPct = dto.getRetentionPct() != null ? dto.getRetentionPct() : BigDecimal.valueOf(5.00);
        BigDecimal tdsPct = dto.getTdsPct() != null ? dto.getTdsPct() : BigDecimal.valueOf(2.00);

        BigDecimal retentionAmount = gross.multiply(retentionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal tdsAmount = gross.multiply(tdsPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netPayable = gross.subtract(retentionAmount).subtract(tdsAmount);

        String billNumber = dto.getBillNumber();
        if (billNumber == null || billNumber.isBlank()) {
            billNumber = "BILL-" + System.currentTimeMillis() % 1000000;
        }

        String billId = "bill_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        ContractorBill bill = ContractorBill.builder()
            .id(billId)
            .project(project)
            .contractorName(dto.getContractorName())
            .billNumber(billNumber)
            .billDate(dto.getBillDate())
            .grossAmount(gross)
            .retentionPct(retentionPct)
            .retentionAmount(retentionAmount)
            .tdsPct(tdsPct)
            .tdsAmount(tdsAmount)
            .netPayable(netPayable)
            .status(BillPaymentStatus.PENDING)
            .workDescription(dto.getWorkDescription())
            .certifiedBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        ContractorBill saved = contractorBillRepository.save(bill);
        return mapToBillDto(saved);
    }

    @Transactional
    public ContractorBillDto updateBillStatus(String projectId, String billId, BillPaymentStatus status, UserPrincipal principal) {
        return updateBillStatus(projectId, billId, status, null, principal);
    }

    @Transactional
    public ContractorBillDto updateBillStatus(
        String projectId,
        String billId,
        BillPaymentStatus status,
        String paymentReference,
        UserPrincipal principal
    ) {
        validateProjectAccess(projectId, principal);

        ContractorBill bill = contractorBillRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Contractor bill not found: " + billId));

        if (!bill.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Contractor bill does not belong to project " + projectId);
        }

        bill.setStatus(status);
        if (status == BillPaymentStatus.PAID && paymentReference != null && !paymentReference.isBlank()) {
            bill.setPaymentReference(paymentReference);
        }

        ContractorBill saved = contractorBillRepository.save(bill);
        return mapToBillDto(saved);
    }

    // -------------------------------------------------------------------------
    // Cost Dashboard & Financial Summary
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public CostDashboardSummaryDto getCostDashboard(String projectId, String period, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        BigDecimal totalBudget = project.getBudgetInr() != null ? project.getBudgetInr() : BigDecimal.ZERO;

        List<Expense> allExpenses = expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId);
        List<ContractorBill> allBills = contractorBillRepository.findByProjectIdOrderByBillDateDesc(projectId);
        List<PurchaseOrder> allPos = purchaseOrderRepository.findByProjectIdOrderByOrderDateDesc(projectId);
        List<Material> materials = materialRepository.findByProjectId(projectId);

        // 1. Actual Cost: Sum of PAID expenses + PAID contractor bills
        BigDecimal paidExpenses = allExpenses.stream()
            .filter(e -> e.getStatus() == ExpenseStatus.PAID)
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidBills = allBills.stream()
            .filter(b -> b.getStatus() == BillPaymentStatus.PAID)
            .map(ContractorBill::getNetPayable)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualCost = paidExpenses.add(paidBills);

        // 2. Committed Cost: Active POs (ISSUED / PARTIALLY_DELIVERED) + APPROVED contractor bills
        BigDecimal poCommitments = allPos.stream()
            .filter(po -> po.getStatus() == PurchaseOrderStatus.ISSUED || po.getStatus() == PurchaseOrderStatus.PARTIALLY_DELIVERED)
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal approvedBillsCommitment = allBills.stream()
            .filter(b -> b.getStatus() == BillPaymentStatus.APPROVED)
            .map(ContractorBill::getNetPayable)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal committedCost = poCommitments.add(approvedBillsCommitment);

        // 3. Remaining Budget & Variance
        BigDecimal remainingBudget = totalBudget.subtract(actualCost);
        BigDecimal projectedFinalCost = actualCost.add(committedCost);

        BigDecimal budgetUsedPct = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPct = actualCost.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 1, RoundingMode.HALF_UP);
        }

        // 4. Category Breakdown
        Map<String, BigDecimal> categorySpend = new LinkedHashMap<>();
        categorySpend.put("MATERIALS", poCommitments.add(
            allExpenses.stream()
                .filter(e -> e.getCategory() == ExpenseCategory.MATERIALS)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
        categorySpend.put("LABOR", allExpenses.stream()
            .filter(e -> e.getCategory() == ExpenseCategory.LABOR)
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        categorySpend.put("SUBCONTRACTOR", allBills.stream()
            .map(ContractorBill::getNetPayable)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        categorySpend.put("EQUIPMENT", allExpenses.stream()
            .filter(e -> e.getCategory() == ExpenseCategory.EQUIPMENT)
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        categorySpend.put("UTILITIES", allExpenses.stream()
            .filter(e -> e.getCategory() == ExpenseCategory.UTILITIES)
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal totalTrackedSpend = categorySpend.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryCostDto> breakdown = new ArrayList<>();
        categorySpend.forEach((catKey, spend) -> {
            BigDecimal pct = BigDecimal.ZERO;
            if (totalTrackedSpend.compareTo(BigDecimal.ZERO) > 0) {
                pct = spend.multiply(BigDecimal.valueOf(100)).divide(totalTrackedSpend, 1, RoundingMode.HALF_UP);
            }
            breakdown.add(CategoryCostDto.builder()
                .category(catKey)
                .categoryName(getCategoryDisplayName(catKey))
                .budgetAmount(totalBudget.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP))
                .actualAmount(spend)
                .percentage(pct)
                .build());
        });

        // 5. Top Material Consumption
        List<MaterialConsumptionDto> topMaterials = new ArrayList<>();
        for (Material mat : materials) {
            BigDecimal unitCost = mat.getUnitCost() != null ? mat.getUnitCost() : BigDecimal.ZERO;
            BigDecimal currentStock = mat.getCurrentStock() != null ? mat.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal estValue = currentStock.multiply(unitCost);

            topMaterials.add(MaterialConsumptionDto.builder()
                .materialId(mat.getId())
                .materialName(mat.getName())
                .unit(mat.getUnit())
                .quantityConsumed(currentStock)
                .totalCost(estValue)
                .consumptionPercentage(BigDecimal.valueOf(10.0))
                .build());
        }

        return CostDashboardSummaryDto.builder()
            .projectId(projectId)
            .projectName(project.getName())
            .period(period != null ? period : "TOTAL")
            .totalBudget(totalBudget)
            .actualCost(actualCost)
            .budgetUsedPct(budgetUsedPct)
            .committedCost(committedCost)
            .remainingBudget(remainingBudget)
            .projectedFinalCost(projectedFinalCost)
            .costBreakdown(breakdown)
            .topMaterialConsumption(topMaterials)
            .build();
    }

    private String getCategoryDisplayName(String catKey) {
        return switch (catKey) {
            case "MATERIALS" -> "Materials (Cement, Steel, Aggregates)";
            case "LABOR" -> "Direct Site Labor & Overtime";
            case "SUBCONTRACTOR" -> "Specialist Contractor RA Bills";
            case "EQUIPMENT" -> "Machinery, Cranes & Piling Rentals";
            case "UTILITIES" -> "Power, Water & Environmental Permits";
            default -> catKey;
        };
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private ExpenseDto mapToExpenseDto(Expense e) {
        return ExpenseDto.builder()
            .id(e.getId())
            .projectId(e.getProject().getId())
            .category(e.getCategory())
            .title(e.getTitle())
            .amount(e.getAmount())
            .expenseDate(e.getExpenseDate())
            .receiptUrl(e.getReceiptUrl())
            .status(e.getStatus())
            .submittedBy(e.getSubmittedBy())
            .verifiedBy(e.getVerifiedBy())
            .approvedBy(e.getApprovedBy())
            .paidBy(e.getPaidBy())
            .paymentReference(e.getPaymentReference())
            .rejectionReason(e.getRejectionReason())
            .idempotencyKey(e.getIdempotencyKey())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    private ContractorBillDto mapToBillDto(ContractorBill b) {
        return ContractorBillDto.builder()
            .id(b.getId())
            .projectId(b.getProject().getId())
            .contractorName(b.getContractorName())
            .billNumber(b.getBillNumber())
            .billDate(b.getBillDate())
            .grossAmount(b.getGrossAmount())
            .retentionPct(b.getRetentionPct())
            .retentionAmount(b.getRetentionAmount())
            .tdsPct(b.getTdsPct())
            .tdsAmount(b.getTdsAmount())
            .netPayable(b.getNetPayable())
            .status(b.getStatus())
            .workDescription(b.getWorkDescription())
            .certifiedBy(b.getCertifiedBy())
            .paymentReference(b.getPaymentReference())
            .idempotencyKey(b.getIdempotencyKey())
            .createdAt(b.getCreatedAt())
            .updatedAt(b.getUpdatedAt())
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
