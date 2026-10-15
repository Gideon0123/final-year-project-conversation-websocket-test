package com.example.stomptestclient;

import java.time.LocalDateTime;

public record PresenceUpdate(
        Long userId,
        boolean online,
        LocalDateTime timestamp
) {
}