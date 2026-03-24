package com.example.backend.dto;

public record CreateSubscriptionPlanRequestDTO(
        String name,
        double price
) {
}
