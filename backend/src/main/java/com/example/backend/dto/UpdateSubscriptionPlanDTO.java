package com.example.backend.dto;

public record UpdateSubscriptionPlanDTO(
        Long id,
        String name,
        Long price,
        int durationDays
) {
}
