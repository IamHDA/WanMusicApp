package com.example.backend.service;

import com.example.backend.dto.CreateSubscriptionPlanRequestDTO;
import com.example.backend.dto.UpdateSubscriptionPlanDTO;

public interface SubscriptionPlanService {

    String updateSubscriptionPlanPrice(UpdateSubscriptionPlanDTO dto);
    String createSubscriptionPlan(CreateSubscriptionPlanRequestDTO dto);
    String deleteSubscriptionPlan(Long subscriptionPlanId);

}
