package com.example.backend.dto;

public record UpdateSubscriptionPlanDTO(
        Long id,
        String name,
        double price
) {
}
