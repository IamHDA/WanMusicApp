package com.example.backend.controller;

import com.example.backend.dto.checkout.CheckoutRequestDTO;
import com.example.backend.dto.checkout.CheckoutResponseDTO;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(@RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhookBody) {
        // IN RA ĐỂ KIỂM CHỨNG PAYOS CÓ THỰC SỰ BẮN VỀ KHÔNG
        log.info("📩 NHẬN ĐƯỢC WEBHOOK TỪ PAYOS: {}", webhookBody.getData() != null ? webhookBody.getData().getOrderCode() : "Không có data");

        try {
            paymentService.handleWebhook(webhookBody);
            log.info("✅ Xử lý Webhook THÀNH CÔNG cho orderCode!");
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ LỖI XỬ LÝ WEBHOOK: {}", e.getMessage());
            return ResponseEntity.ok("OK");
        }
    }
}