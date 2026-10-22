package com.example.contruction_management_app.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateDto {
    private String poNumber;

    @NotBlank
    private String vendorId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate deliveryDeadline;

    @NotEmpty
    @Valid
    private List<PurchaseOrderItemCreateDto> items;
}
