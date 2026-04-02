package com.example.backend.service;


import com.example.backend.dto.subscription.UserSubscriptionRequestDTO;

public interface SubscriptionService {
    UserSubscriptionRequestDTO getUserCurrentSubscription();
}
