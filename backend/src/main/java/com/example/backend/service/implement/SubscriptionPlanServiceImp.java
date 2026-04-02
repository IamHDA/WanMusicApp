package com.example.backend.service.implement;

import com.example.backend.dto.CreateSubscriptionPlanRequestDTO;
import com.example.backend.dto.SubscriptionPlanDTO;
import com.example.backend.dto.UpdateSubscriptionPlanDTO;
import com.example.backend.entity.SubscriptionPlan;
import com.example.backend.repository.SubscriptionPlanRepository;
import com.example.backend.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImp implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSubscriptionPlanPrice(UpdateSubscriptionPlanDTO dto) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepo.findById(dto.id()).orElseThrow(() -> new RuntimeException("Subscription Plan not found!"));
        if(dto.price() != subscriptionPlan.getPrice()) {
            subscriptionPlan.setPrice(dto.price());
        }

        if(dto.name() != null && !dto.name().isBlank() && !subscriptionPlan.getName().equals(dto.name())) {
            subscriptionPlan.setName(dto.name());
        }
        if (dto.durationDays() > 0 && dto.durationDays() != subscriptionPlan.getDurationDays()){
            subscriptionPlan.setDurationDays(dto.durationDays());
        }

        subscriptionPlanRepo.save(subscriptionPlan);

        return "Updated subscription plan successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSubscriptionPlan(CreateSubscriptionPlanRequestDTO dto) {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();

        subscriptionPlan.setName(dto.name());
        subscriptionPlan.setPrice(dto.price());
        subscriptionPlan.setDurationDays(dto.durationDays());

        subscriptionPlanRepo.save(subscriptionPlan);

        return "Subscription plan created successfully!";
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteSubscriptionPlan(Long subscriptionPlanId) {
        subscriptionPlanRepo.deleteById(subscriptionPlanId);
        return "Subscription plan deleted successfully!";
    }

    @Override
    public List<SubscriptionPlanDTO> getAllPlans() {
        return subscriptionPlanRepo.findAll().stream()
                .map(plan -> {
                    SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
                    dto.setId(plan.getId());
                    dto.setName(plan.getName());
                    dto.setPrice(plan.getPrice());
                    dto.setDurationDays(plan.getDurationDays());
                    return dto;
                }).toList();
    }
}
