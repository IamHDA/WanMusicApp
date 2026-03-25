package com.example.backend.service;

import com.example.backend.dto.CreateSubscriptionPlanRequestDTO;
import com.example.backend.dto.SubscriptionPlanDTO;
import com.example.backend.dto.UpdateSubscriptionPlanDTO;

import java.util.List;

public interface SubscriptionPlanService {

    String updateSubscriptionPlanPrice(UpdateSubscriptionPlanDTO dto);
    String createSubscriptionPlan(CreateSubscriptionPlanRequestDTO dto);
    String deleteSubscriptionPlan(Long subscriptionPlanId);
    List<SubscriptionPlanDTO> getAllPlans();
}
