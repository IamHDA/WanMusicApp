package com.example.backend.dto.subscription;

public record UpdateSubscriptionPlanDTO(
        Long id,
        String name,
        Long price,
        int durationDays
) {
}
