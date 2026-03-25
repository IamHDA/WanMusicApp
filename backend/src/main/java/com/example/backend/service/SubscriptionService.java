package com.example.backend.service;


import com.example.backend.dto.subscription.UserSubscriptionDTO;

public interface SubscriptionService {
    String subscribe(Long planId);
    UserSubscriptionDTO getUserCurrentSubscription();
}
