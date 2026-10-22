package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.Vendor;

public record VendorDto(
    String id,
    String organizationId,
    String name,
    String gstin,
    String phone,
    String email,
    String category,
    String paymentTerms
) {
    public static VendorDto fromEntity(Vendor v) {
        return new VendorDto(
            v.getId(),
            v.getOrganization().getId(),
            v.getName(),
            v.getGstin(),
            v.getPhone(),
            v.getEmail(),
            v.getCategory(),
            v.getPaymentTerms()
        );
    }
}
