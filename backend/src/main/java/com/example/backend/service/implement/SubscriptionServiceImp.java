package com.example.backend.service.implement;

import com.example.backend.Enum.SubscriptionType;
import com.example.backend.dto.subscription.UserSubscriptionRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Subscription;
import com.example.backend.entity.SubscriptionPlan;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.SubscriptionPlanRepository;
import com.example.backend.repository.SubscriptionRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImp implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepo;
    private final MemberRepository memberRepo;
    private final SubscriptionPlanRepository planRepo;
    private final AuthenticationService authenticationService;


    @Override
    public UserSubscriptionRequestDTO getUserCurrentSubscription() {
        Long currentUserId = authenticationService.getCurrentMemberId();

        // Tìm subscription đang active của user
        Subscription sub = subscriptionRepo.findActiveByMemberId(currentUserId).orElse(null);

        UserSubscriptionRequestDTO dto = new UserSubscriptionRequestDTO();

        if (sub == null) {
            // Chưa có subscription => trả về FREE mặc định
            dto.setId(null);
            dto.setPlanName("Free");
            dto.setSubscriptionType("FREE");
            dto.setPrice(0);
            dto.setStartDate(null);
            dto.setEndDate(null);
            dto.setActive(false);
            return dto;
        }

        dto.setId(sub.getId());
        dto.setPlanName(sub.getPlan().getName());
        dto.setSubscriptionType("PREMIUM");
        dto.setPrice(sub.getPlan().getPrice());
        dto.setStartDate(sub.getStartDate());
        dto.setEndDate(sub.getEndDate());
        dto.setActive(sub.isActive());
        return dto;
    }
}
