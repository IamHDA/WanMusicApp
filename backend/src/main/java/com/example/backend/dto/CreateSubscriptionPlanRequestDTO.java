package com.example.backend.dto;

public record CreateSubscriptionPlanRequestDTO(
        String name,
        Long price,
        int durationDays
) {
}
