package com.example.backend.repository;

import com.example.backend.entity.Subscription;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s WHERE s.subscriber.id = :memberId AND s.isActive = true")
    Optional<Subscription> findActiveByMemberId(@Param("memberId") Long memberId);

    // Tìm các gói đang kích hoạt (isActive = true) và có ngày hết hạn nhỏ hơn một ngày truyền vào
    List<Subscription> findAllByIsActiveTrueAndEndDateBefore(LocalDate currentDate);
}
