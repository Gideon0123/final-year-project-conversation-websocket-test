package com.example.stomptestclient;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        LocalDateTime createdAt,
        Long recipientId,
        LocalDateTime deliveredAt,
        LocalDateTime readAt
) {
}