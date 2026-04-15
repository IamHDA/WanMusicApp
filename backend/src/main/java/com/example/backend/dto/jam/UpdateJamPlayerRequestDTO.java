package com.example.backend.dto.jam;

import com.example.backend.Enum.InteractionType;

public record UpdateJamPlayerRequestDTO (
        Long jamId,
        Long jamNotificationId,
        Long trackId,
        InteractionType interactionType,
        Integer seekPosition,
        Float playbackRate
) {
}
