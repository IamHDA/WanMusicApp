package com.example.backend.entity;

import com.example.backend.Enum.Role;
import com.example.backend.Enum.SubscriptionType;
import com.example.backend.Enum.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 30, nullable = false)
    private String email;
    @Column(length = 50, nullable = false)
    private String password;
    @Column(name = "display_name", length = 20, nullable = false)
    private String displayName;
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private SubscriptionType subscriptionType;
    @Column(nullable = false)
    private UserStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Token> tokens;
}
