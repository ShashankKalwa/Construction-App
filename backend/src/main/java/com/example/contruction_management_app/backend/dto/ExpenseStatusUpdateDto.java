package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.ExpenseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseStatusUpdateDto {
    @NotNull
    private ExpenseStatus status;
}
