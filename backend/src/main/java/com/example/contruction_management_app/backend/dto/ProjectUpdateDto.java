package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record ProjectUpdateDto(
    @NotBlank String name,
    String location,
    BigDecimal budgetInr,
    String projectType,
    String clientName,
    String startDate,
    String endDate
) {}
