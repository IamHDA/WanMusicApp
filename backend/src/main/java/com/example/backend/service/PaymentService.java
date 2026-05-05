package com.example.backend.service;


import com.example.backend.dto.checkout.CheckoutRequestDTO;
import com.example.backend.dto.checkout.CheckoutResponseDTO;
import org.springframework.web.bind.annotation.RequestBody;
import vn.payos.model.webhooks.Webhook;

public interface PaymentService {
    CheckoutResponseDTO createPayment(CheckoutRequestDTO request);
    void handleWebhook(Webhook webhook);
}

