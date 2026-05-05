package com.example.backend.entity;

import com.example.backend.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long orderCode;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(length = 1000)
    private String paymentLink;

    private LocalDateTime createAt;
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // --- Snapshot thông tin gói tại thời điểm mua ---
    private Long planId;
    private String planName;
    private long planPrice;
    private int durationDays;

    @PrePersist
    protected void onCreate(){
        createAt = LocalDateTime.now();
        if (status == null);
            status = PaymentStatus.PENDING;
    }

}
