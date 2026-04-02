package com.example.backend.service;

import com.example.backend.Enum.SubscriptionType;
import com.example.backend.entity.Member;
import com.example.backend.entity.Subscription;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCleanupService {
    private final SubscriptionRepository subscriptionRepo;
    private final MemberRepository memberRepo;

    /**
     * Hàm sẽ tự động chạy trong 2 trường hợp:
     * 1. Ngay khi Server Spring Boot vừa khởi động xong (@EventListener)
     * 2. Chạy định kỳ mỗi 1 tiếng đồng hồ (phút thứ 0 của mỗi giờ) (@Scheduled)
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 * * * *") // Cron: giây 0, phút 0, mọi giờ, mọi ngày
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredSubscriptions() {
        log.info("[CRON JOB] Bắt đầu quét các gói Premium đã hết hạn...");

        LocalDate today = LocalDate.now();
        List<Subscription> expiredSubs = subscriptionRepo.findAllByIsActiveTrueAndEndDateBefore(today);

        if (expiredSubs.isEmpty()) {
            log.info("[CRON JOB] Không có gói Premium nào hết hạn cần xử lý.");
            return;
        }

        for (Subscription sub : expiredSubs) {
            sub.setActive(false);
            Member member = sub.getSubscriber();
            if (member.getSubscriptionType() == SubscriptionType.PREMIUM) {
                member.setSubscriptionType(SubscriptionType.FREE);
                memberRepo.save(member);
                log.info("Đã hạ cấp user {} về FREE do gói hết hạn vào ngày {}", member.getId(), sub.getEndDate());
            }
        }

        // Lưu toàn bộ trạng thái false vào DB
        subscriptionRepo.saveAll(expiredSubs);
        log.info("[CRON JOB] Đã xử lý hạ cấp thành công {} tài khoản.", expiredSubs.size());
    }

}
