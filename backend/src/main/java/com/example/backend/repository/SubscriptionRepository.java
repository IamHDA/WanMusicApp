package com.example.backend.repository;

import com.example.backend.entity.Subscription;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s WHERE s.subscriber.id = :memberId AND s.isActive = true")
    Optional<Subscription> findActiveByMemberId(@Param("memberId") Long memberId);
}
