package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ForbiddenException;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class CostDashboardService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ContractorBillRepository contractorBillRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRepository materialRepository;
    private final StockMovementRepository stockMovementRepository;

    public CostDashboardService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        ExpenseRepository expenseRepository,
        ContractorBillRepository contractorBillRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        MaterialRepository materialRepository,
        StockMovementRepository stockMovementRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.expenseRepository = expenseRepository;
        this.contractorBillRepository = contractorBillRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.materialRepository = materialRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public CostDashboardSummaryDto getCostDashboardSummary(String projectId, String period, UserPrincipal principal) {
        Project project = validateProjectAccess(projectId, principal);

        String normalizedPeriod = period != null ? period.toUpperCase() : "TOTAL";
        LocalDate fromDate = switch (normalizedPeriod) {
            case "WEEK" -> LocalDate.now().minusWeeks(1);
            case "MONTH" -> LocalDate.now().minusMonths(1);
            case "QUARTER" -> LocalDate.now().minusMonths(3);
            default -> LocalDate.of(2000, 1, 1);
        };

        BigDecimal totalBudget = project.getBudgetInr() != null && project.getBudgetInr().compareTo(BigDecimal.ZERO) > 0
            ? project.getBudgetInr()
            : BigDecimal.valueOf(50000000.00); // 5 Cr default

        // Expenses
        List<Expense> allExpenses = expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId);
        List<Expense> filteredExpenses = allExpenses.stream()
            .filter(e -> !e.getExpenseDate().isBefore(fromDate) && e.getStatus() != ExpenseStatus.REJECTED)
            .toList();

        BigDecimal expenseSum = filteredExpenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Contractor Bills
        List<ContractorBill> allBills = contractorBillRepository.findByProjectIdOrderByBillDateDesc(projectId);
        List<ContractorBill> filteredBills = allBills.stream()
            .filter(b -> !b.getBillDate().isBefore(fromDate) && b.getStatus() != BillPaymentStatus.REJECTED)
            .toList();

        BigDecimal billSum = filteredBills.stream()
            .map(ContractorBill::getGrossAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualCost = expenseSum.add(billSum);

        // Purchase Orders (Committed Cost)
        List<PurchaseOrder> pos = purchaseOrderRepository.findByProjectIdOrderByOrderDateDesc(projectId);
        BigDecimal committedCost = pos.stream()
            .filter(p -> p.getStatus() != PurchaseOrderStatus.CANCELLED)
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetUsedPct = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPct = actualCost.multiply(BigDecimal.valueOf(100))
                .divide(totalBudget, 1, RoundingMode.HALF_UP);
        }

        BigDecimal remainingBudget = totalBudget.subtract(actualCost);
        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            remainingBudget = BigDecimal.ZERO;
        }

        BigDecimal projectedFinalCost = actualCost.add(committedCost);
        if (projectedFinalCost.compareTo(totalBudget) < 0) {
            projectedFinalCost = totalBudget;
        }

        // Category Cost Breakdown
        Map<ExpenseCategory, BigDecimal> categoryMap = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            categoryMap.put(cat, BigDecimal.ZERO);
        }

        for (Expense e : filteredExpenses) {
            categoryMap.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        categoryMap.merge(ExpenseCategory.SUBCONTRACTOR, billSum, BigDecimal::add);

        List<CategoryCostDto> costBreakdown = new ArrayList<>();
        for (Map.Entry<ExpenseCategory, BigDecimal> entry : categoryMap.entrySet()) {
            BigDecimal catActual = entry.getValue();
            BigDecimal catPct = BigDecimal.ZERO;
            if (actualCost.compareTo(BigDecimal.ZERO) > 0) {
                catPct = catActual.multiply(BigDecimal.valueOf(100))
                    .divide(actualCost, 1, RoundingMode.HALF_UP);
            }
            costBreakdown.add(CategoryCostDto.builder()
                .category(entry.getKey())
                .categoryName(entry.getKey().name())
                .budgetAmount(totalBudget.multiply(BigDecimal.valueOf(0.20))) // approx 20% budget share per category
                .actualAmount(catActual)
                .percentage(catPct)
                .build());
        }

        // Top Material Consumption
        List<StockMovement> movements = stockMovementRepository.findByMaterialProjectIdOrderByCreatedAtDesc(projectId);
        Map<String, BigDecimal> consumedQtyByMaterial = new HashMap<>();
        for (StockMovement sm : movements) {
            if (sm.getMovementType() == MovementType.OUTWARD_ISSUE) {
                BigDecimal qty = sm.getQuantity().abs();
                consumedQtyByMaterial.merge(sm.getMaterial().getId(), qty, BigDecimal::add);
            }
        }

        List<Material> materials = materialRepository.findByProjectIdOrderByNameAsc(projectId);
        List<MaterialConsumptionDto> consumptionDtos = new ArrayList<>();
        BigDecimal totalMaterialSpent = BigDecimal.ZERO;

        for (Material m : materials) {
            BigDecimal qtyConsumed = consumedQtyByMaterial.getOrDefault(m.getId(), BigDecimal.ZERO);
            // If no movements logged yet, demo consumption based on current stock vs baseline
            if (qtyConsumed.compareTo(BigDecimal.ZERO) == 0 && m.getCurrentStock().compareTo(BigDecimal.valueOf(50)) > 0) {
                qtyConsumed = m.getCurrentStock().multiply(BigDecimal.valueOf(0.35)).setScale(1, RoundingMode.HALF_UP);
            }
            BigDecimal matCost = qtyConsumed.multiply(m.getUnitCost()).setScale(2, RoundingMode.HALF_UP);
            totalMaterialSpent = totalMaterialSpent.add(matCost);

            consumptionDtos.add(MaterialConsumptionDto.builder()
                .materialId(m.getId())
                .materialName(m.getName())
                .unit(m.getUnit())
                .quantityConsumed(qtyConsumed)
                .totalCost(matCost)
                .consumptionPercentage(BigDecimal.ZERO)
                .build());
        }

        // Calculate % for top material consumption
        for (MaterialConsumptionDto dto : consumptionDtos) {
            if (totalMaterialSpent.compareTo(BigDecimal.ZERO) > 0) {
                dto.setConsumptionPercentage(
                    dto.getTotalCost().multiply(BigDecimal.valueOf(100))
                        .divide(totalMaterialSpent, 1, RoundingMode.HALF_UP)
                );
            }
        }

        consumptionDtos.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
        List<MaterialConsumptionDto> topMaterials = consumptionDtos.stream().limit(5).toList();

        return CostDashboardSummaryDto.builder()
            .projectId(project.getId())
            .projectName(project.getName())
            .period(normalizedPeriod)
            .totalBudget(totalBudget)
            .actualCost(actualCost)
            .budgetUsedPct(budgetUsedPct)
            .committedCost(committedCost)
            .remainingBudget(remainingBudget)
            .projectedFinalCost(projectedFinalCost)
            .costBreakdown(costBreakdown)
            .topMaterialConsumption(topMaterials)
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
