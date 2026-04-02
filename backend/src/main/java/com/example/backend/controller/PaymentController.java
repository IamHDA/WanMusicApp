package com.example.backend.controller;

import com.example.backend.dto.checkout.CheckoutRequestDTO;
import com.example.backend.dto.checkout.CheckoutResponseDTO;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(@RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhookBody) {
        try{
            paymentService.handleWebhook(webhookBody);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid webhook");
        }

    }

}
