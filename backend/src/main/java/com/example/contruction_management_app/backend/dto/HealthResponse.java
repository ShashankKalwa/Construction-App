package com.example.contruction_management_app.backend.dto;

public record HealthResponse(
    String status,
    String service,
    String version,
    long timestamp
) {
    public HealthResponse(String status, String service, String version) {
        this(status, service, version, System.currentTimeMillis());
    }
}
