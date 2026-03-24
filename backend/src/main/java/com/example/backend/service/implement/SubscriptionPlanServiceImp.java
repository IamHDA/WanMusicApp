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
    public String updateSubscriptionPlanPrice(UpdateSubscriptionPlanDTO dto) {
        Optional<SubscriptionPlan> subscriptionPlan = subscriptionPlanRepo.findById(dto.id());
        if(dto.price() != subscriptionPlan.get().getPrice())
            subscriptionPlan.get().setPrice(dto.price());

        if(!dto.name().isBlank() && !subscriptionPlan.get().getName().equals(dto.name()))
            subscriptionPlan.get().setName(dto.name());

        subscriptionPlanRepo.save(subscriptionPlan.get());

        return "Updated subscription plan successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSubscriptionPlan(CreateSubscriptionPlanRequestDTO dto) {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();

        subscriptionPlan.setName(dto.name());
        subscriptionPlan.setPrice(dto.price());

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
                    return dto;
                }).toList();
    }
}
