package com.example.backend.controller;

import com.example.backend.dto.subscription.UserSubscriptionRequestDTO;
import com.example.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/my")
    public ResponseEntity<UserSubscriptionRequestDTO> getMySubscription() {
        return ResponseEntity.ok(subscriptionService.getUserCurrentSubscription());
    }
}
