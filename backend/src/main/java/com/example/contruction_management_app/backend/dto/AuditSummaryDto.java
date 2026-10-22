package com.example.contruction_management_app.backend.dto;

public record AuditSummaryDto(
    long totalEvents,
    long approvalsCount,
    long stockIssuesCount,
    long documentsCount,
    long paymentsCount
) {}
