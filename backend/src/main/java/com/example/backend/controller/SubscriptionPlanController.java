package com.example.backend.controller;

import com.example.backend.dto.CreateSubscriptionPlanRequestDTO;
import com.example.backend.dto.UpdateSubscriptionPlanDTO;
import com.example.backend.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription-plan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/create")
    public ResponseEntity<String> createSubscriptionPlan(@RequestBody CreateSubscriptionPlanRequestDTO request){
        return ResponseEntity.ok(subscriptionPlanService.createSubscriptionPlan(request));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateSubscriptionPlan(@RequestBody UpdateSubscriptionPlanDTO request){
        return ResponseEntity.ok(subscriptionPlanService.updateSubscriptionPlanPrice(request));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteSubscriptionPlan(@RequestParam Long id){
        return ResponseEntity.ok(subscriptionPlanService.deleteSubscriptionPlan(id));
    }

}
