package com.example.backend.service.implement;

import com.example.backend.Enum.SubscriptionType;
import com.example.backend.dto.subscription.UserSubscriptionDTO;
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
    @Transactional(rollbackFor = Exception.class)
    public String subscribe(Long planId) {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Member member = memberRepo.findById(currentUserId).orElseThrow(() -> new RuntimeException("Member not found!"));
        SubscriptionPlan plan = planRepo.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found!"));

        Subscription subscription = new Subscription();
        subscription.setSubscriber(member);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(1)); // Gói 1 tháng
        subscription.setActive(true);
        subscriptionRepo.save(subscription);

        member.setSubscriptionType(SubscriptionType.PREMIUM);
        memberRepo.save(member);
        return "Subscribed to plan: " + plan.getName() + " successfully!";
    }

    @Override
    public UserSubscriptionDTO getUserCurrentSubscription() {
        Long currentUserId = authenticationService.getCurrentMemberId();
        // Tìm subscription đang active của user
        Subscription sub = subscriptionRepo.findActiveByMemberId(currentUserId).orElseThrow(() -> new RuntimeException("No active subscription found!"));

        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        dto.setId(sub.getId());
        dto.setPlanName(sub.getPlan().getName());
        dto.setPrice(sub.getPlan().getPrice());
        dto.setStartDate(sub.getStartDate());
        dto.setEndDate(sub.getEndDate());
        dto.setActive(sub.isActive());
        return dto;
    }
}
