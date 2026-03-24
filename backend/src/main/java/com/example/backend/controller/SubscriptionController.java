package com.example.backend.controller;

import com.example.backend.dto.subscription.SubscribeRequestDTO;
import com.example.backend.dto.subscription.UserSubscriptionDTO;
import com.example.backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<String>  subscribe(@RequestBody SubscribeRequestDTO dto) {
        return ResponseEntity.ok(subscriptionService.subscribe(dto.planId()));
    }

    @GetMapping("/my")
    public ResponseEntity<UserSubscriptionDTO> getMySubscription() {
        return ResponseEntity.ok(subscriptionService.getUserCurrentSubscription());
    }
}
