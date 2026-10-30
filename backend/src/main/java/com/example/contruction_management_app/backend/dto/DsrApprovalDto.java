package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DsrApprovalDto(
    @NotBlank String action, // APPROVE, REJECT
    String notes
) {}
