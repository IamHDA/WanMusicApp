package com.example.backend.dto.subscription;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserSubscriptionDTO {
    private Long id;
    private String planName;
    private double price;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
}
