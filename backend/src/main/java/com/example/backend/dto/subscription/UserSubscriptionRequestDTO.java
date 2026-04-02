package com.example.backend.dto.subscription;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserSubscriptionRequestDTO {
    private Long id;
    private String planName;
    private String subscriptionType;
    private double price;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
}
