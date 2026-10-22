package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.BadRequestException;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.ContractorBillRepository;
import com.example.contruction_management_app.backend.repository.ExpenseRepository;
import com.example.contruction_management_app.backend.repository.ProjectMemberRepository;
import com.example.contruction_management_app.backend.repository.ProjectRepository;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FinanceService {

    private final ExpenseRepository expenseRepository;
    private final ContractorBillRepository contractorBillRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public FinanceService(
        ExpenseRepository expenseRepository,
        ContractorBillRepository contractorBillRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.contractorBillRepository = contractorBillRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

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
            .status(ExpenseStatus.SUBMITTED)
            .submittedBy(principal.getId())
            .createdAt(Instant.now())
            .build();

        Expense saved = expenseRepository.save(expense);
        return mapToExpenseDto(saved);
    }

    @Transactional
    public ExpenseDto updateExpenseStatus(String projectId, String expenseId, ExpenseStatus status, UserPrincipal principal) {
        validateProjectAccess(projectId, principal);

        Expense exp = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));

        if (!exp.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Expense does not belong to project " + projectId);
        }

        exp.setStatus(status);
        switch (status) {
            case VERIFIED -> exp.setVerifiedBy(principal.getId());
            case APPROVED -> exp.setApprovedBy(principal.getId());
            case PAID -> exp.setPaidBy(principal.getId());
            default -> {}
        }

        Expense saved = expenseRepository.save(exp);
        return mapToExpenseDto(saved);
    }

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
        validateProjectAccess(projectId, principal);

        ContractorBill bill = contractorBillRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Contractor bill not found: " + billId));

        if (!bill.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Contractor bill does not belong to project " + projectId);
        }

        bill.setStatus(status);
        ContractorBill saved = contractorBillRepository.save(bill);
        return mapToBillDto(saved);
    }

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
