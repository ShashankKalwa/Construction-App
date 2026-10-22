package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorCreateDto(
    @NotBlank(message = "Vendor name is required")
    String name,

    String gstin,
    String phone,
    String email,
    String category,
    String paymentTerms
) {}
