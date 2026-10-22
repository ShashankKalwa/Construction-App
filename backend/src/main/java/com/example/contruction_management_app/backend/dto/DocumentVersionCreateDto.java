package com.example.contruction_management_app.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentVersionCreateDto(
    @NotBlank String versionNumber,
    @NotBlank String fileName,
    Long fileSizeBytes,
    String mimeType,
    @NotBlank String fileUrl,
    String changeNotes
) {}
