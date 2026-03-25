package com.example.backend.dto;

import com.example.backend.Enum.NotificationType;

import java.time.LocalDateTime;

public record JamNotificationDTO (
        Long jamNotificationId,
        String message,
        NotificationType type,
        LocalDateTime createdAt
){
}
