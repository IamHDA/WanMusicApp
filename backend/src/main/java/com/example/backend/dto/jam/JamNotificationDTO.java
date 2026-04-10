package com.example.backend.dto.jam;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.NotificationType;

import java.time.LocalDateTime;

public record JamNotificationDTO (
        Long jamSessionId,
        Long jamNotificationId,
        Long trackId,
        String message,
        NotificationType type,
        InteractionType interactionType,
        Integer duration,
        LocalDateTime createdAt
){
}
