package com.example.contruction_management_app.backend.exception;

public record ErrorResponse(
    int status,
    String error,
    String message,
    long timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, System.currentTimeMillis());
    }
}
