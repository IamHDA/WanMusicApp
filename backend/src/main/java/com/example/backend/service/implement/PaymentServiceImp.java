package com.example.backend.service.implement;

import com.example.backend.Enum.PaymentStatus;
import com.example.backend.Enum.SubscriptionType;
import com.example.backend.dto.checkout.CheckoutRequestDTO;
import com.example.backend.dto.checkout.CheckoutResponseDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Payment;
import com.example.backend.entity.Subscription;
import com.example.backend.entity.SubscriptionPlan;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.SubscriptionPlanRepository;
import com.example.backend.repository.SubscriptionRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {
    private final PayOS payOS;
    private final PaymentRepository paymentRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final SubscriptionPlanRepository subscriptionPlanRepo;
    private final MemberRepository memberRepo;
    private final AuthenticationService authenticationService;

    @Value("${app.frontend.url}")
    private String returnUrl;

    @Value("${app.frontend.cancel-url}")
    private String cancelUrl;


    @Override
    @Transactional
    public CheckoutResponseDTO createPayment(CheckoutRequestDTO request) {
        Long memberId = authenticationService.getCurrentMemberId();
        Member member = memberRepo.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found!"));

        if (member.getSubscriptionType() == SubscriptionType.PREMIUM) {
            throw new RuntimeException("You are Preminum already!");
        }
        SubscriptionPlan plan = subscriptionPlanRepo.findById(request.planId()).orElseThrow(() -> new RuntimeException("SubscriptionPlan not found!"));

        Long orderCode = System.currentTimeMillis();
        // Save Payment into DB with PENDING status
        Payment payment = new Payment();
        payment.setOrderCode(orderCode);
        payment.setAmount(plan.getPrice());
        payment.setMember(member);
        payment.setPlanId(plan.getId());
        payment.setPlanName(plan.getName());
        payment.setPlanPrice(plan.getPrice());
        payment.setDurationDays(plan.getDurationDays());
        paymentRepo.save(payment);

        try {
            // Call PayOS SDK
            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(plan.getPrice())
                    .description("Đơn hàng: " + orderCode)
                    .cancelUrl(cancelUrl)
                    .returnUrl(returnUrl)
                    .build();

            var paymentLink = payOS.paymentRequests().create(paymentRequest);
            // Update paymentLink into DB
            payment.setPaymentLink(paymentLink.getCheckoutUrl());
            paymentRepo.save(payment);

            return new CheckoutResponseDTO(paymentLink.getCheckoutUrl());
        } catch (Exception e) {
            log.error("PayOS Error: ", e);
            throw new RuntimeException("Lỗi tạo giao dịch thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // QUAN TRỌNG: Bắt buộc phải có để tránh rác dữ liệu
    public void handleWebhook(Webhook webhookBody) {
        try {
            // 1. Verify webhook payOS
            WebhookData data = payOS.webhooks().verify(webhookBody);

            Payment payment = paymentRepo.findByOrderCode(data.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("Payment not found!"));

            // 2. Idempotent check: skip if it's already processed
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return;
            }

            // 3. Nếu thanh toán thành công (PayOS thường trả về code "00")
            if ("00".equals(data.getCode()) || "PAID".equals(data.getCode())) {
                // Update payment
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepo.save(payment);

                Member member = payment.getMember();
                SubscriptionPlan plan = subscriptionPlanRepo.findById(payment.getPlanId())
                        .orElseThrow(() -> new RuntimeException("Subcription plan not found!"));

                // Create Subcription
                Subscription subscription = new Subscription();
                subscription.setSubscriber(member);
                subscription.setPlan(plan);
                subscription.setPayment(payment);
                subscription.setStartDate(LocalDate.now());
                subscription.setEndDate(LocalDate.now().plusDays(plan.getDurationDays()));
                subscription.setActive(true);
                subscriptionRepo.save(subscription);

                // Update Member
                member.setSubscriptionType(SubscriptionType.PREMIUM);
                memberRepo.save(member);

                log.info("Payment success and Subscription created for user: {}", member.getId());
            }
            // 4. NẾU THẤT BẠI HOẶC BỊ HUỶ
            else {
                log.warn("Payment failed or cancelled. PayOS Code: {}", data.getCode());
                // Kiểm tra desc của PayOS, nếu chứa chữ cancel thì gán CANCELLED, ngược lại FAILED
                if (data.getDesc() != null && data.getDesc().toLowerCase().contains("cancel")) {
                    payment.setStatus(PaymentStatus.CANCELLED);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                }
                paymentRepo.save(payment);
            }

        } catch (Exception e) {
            log.error("Webhook processing failed: ", e);
            // QUAN TRỌNG: Phải ném lỗi ra để Spring Boot rollback lại các lệnh save() trước đó nếu có
            throw new RuntimeException("Lỗi khi xử lý Webhook: " + e.getMessage());
        }
    }
}