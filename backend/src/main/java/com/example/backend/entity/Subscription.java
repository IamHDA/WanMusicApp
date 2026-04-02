package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "subscription", indexes = {
        @Index(name = "idx_subscription_active_enddate", columnList = "is_active, end_date")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;
    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "subscriber_id")
    private Member subscriber;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;
}
