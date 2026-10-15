package com.example.stomptestclient;

public record StompErrorResponse(
        String code,
        String message,
        java.time.LocalDateTime timestamp
) {
}