package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.Expense;
import com.example.contruction_management_app.backend.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByProjectIdOrderByExpenseDateDesc(String projectId);
    List<Expense> findByProjectIdAndCategoryOrderByExpenseDateDesc(String projectId, ExpenseCategory category);
}
