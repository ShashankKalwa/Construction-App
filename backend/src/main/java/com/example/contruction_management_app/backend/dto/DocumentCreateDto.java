package com.example.contruction_management_app.backend.dto;

import com.example.contruction_management_app.backend.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentCreateDto(
    @NotBlank String title,
    @NotNull DocumentType documentType,
    String category,
    String versionNumber,
    @NotBlank String fileName,
    Long fileSizeBytes,
    String mimeType,
    @NotBlank String fileUrl,
    String changeNotes
) {}
