package com.example.contruction_management_app.backend.dto;

public record MaterialRequestIssueDto(
    String notes,
    String idempotencyKey
) {}

