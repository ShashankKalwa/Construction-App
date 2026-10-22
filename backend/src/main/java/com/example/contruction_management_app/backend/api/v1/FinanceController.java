package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.domain.ExpenseCategory;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/projects/{projectId}/expenses")
    public ResponseEntity<List<ExpenseDto>> getExpenses(
        @PathVariable String projectId,
        @RequestParam(required = false) ExpenseCategory category,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.getExpenses(projectId, category, principal));
    }

    @GetMapping("/projects/{projectId}/expenses/{expenseId}")
    public ResponseEntity<ExpenseDto> getExpense(
        @PathVariable String projectId,
        @PathVariable String expenseId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.getExpense(projectId, expenseId, principal));
    }

    @PostMapping("/projects/{projectId}/expenses")
    public ResponseEntity<ExpenseDto> createExpense(
        @PathVariable String projectId,
        @Valid @RequestBody ExpenseCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ExpenseDto created = financeService.createExpense(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/projects/{projectId}/expenses/{expenseId}/status")
    public ResponseEntity<ExpenseDto> updateExpenseStatus(
        @PathVariable String projectId,
        @PathVariable String expenseId,
        @Valid @RequestBody ExpenseStatusUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.updateExpenseStatus(projectId, expenseId, dto.getStatus(), principal));
    }

    @GetMapping("/projects/{projectId}/contractor-bills")
    public ResponseEntity<List<ContractorBillDto>> getContractorBills(
        @PathVariable String projectId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.getContractorBills(projectId, principal));
    }

    @GetMapping("/projects/{projectId}/contractor-bills/{billId}")
    public ResponseEntity<ContractorBillDto> getContractorBill(
        @PathVariable String projectId,
        @PathVariable String billId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.getContractorBill(projectId, billId, principal));
    }

    @PostMapping("/projects/{projectId}/contractor-bills")
    public ResponseEntity<ContractorBillDto> createContractorBill(
        @PathVariable String projectId,
        @Valid @RequestBody ContractorBillCreateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ContractorBillDto created = financeService.createContractorBill(projectId, dto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/projects/{projectId}/contractor-bills/{billId}/status")
    public ResponseEntity<ContractorBillDto> updateBillStatus(
        @PathVariable String projectId,
        @PathVariable String billId,
        @Valid @RequestBody BillPaymentStatusUpdateDto dto,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(financeService.updateBillStatus(projectId, billId, dto.getStatus(), principal));
    }
}
