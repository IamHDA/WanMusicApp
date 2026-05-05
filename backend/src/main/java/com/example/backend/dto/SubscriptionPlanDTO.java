package com.example.backend.dto;

import lombok.Data;

@Data
public class SubscriptionPlanDTO {
    private Long id;
    private String name;
    private double price;
    private int durationDays;

}
