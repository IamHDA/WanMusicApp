package com.example.backend.dto;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.NotificationType;

public record CreateJamNotificationRequestDTO(
        Long jamId,
        Long trackId,
        NotificationType notificationType,
        InteractionType interactionType,
        int duration
) {
}
