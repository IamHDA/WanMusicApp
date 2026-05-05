package com.example.backend.controller;

import com.example.backend.dto.CreateSubscriptionPlanRequestDTO;
import com.example.backend.dto.SubscriptionPlanDTO;
import com.example.backend.dto.subscription.UpdateSubscriptionPlanDTO;
import com.example.backend.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plan")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAllPlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllPlans());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createSubscriptionPlan(@RequestBody CreateSubscriptionPlanRequestDTO request){
        return ResponseEntity.ok(subscriptionPlanService.createSubscriptionPlan(request));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSubscriptionPlan(@RequestBody UpdateSubscriptionPlanDTO request){
        return ResponseEntity.ok(subscriptionPlanService.updateSubscriptionPlanPrice(request));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSubscriptionPlan(@RequestParam Long id){
        return ResponseEntity.ok(subscriptionPlanService.deleteSubscriptionPlan(id));
    }

}
