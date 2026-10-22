package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BoqDecisionDto(
    @NotBlank(message = "Decision is required (APPROVED or REJECTED)")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Decision must be APPROVED or REJECTED")
    String decision,

    String comments
) {}
