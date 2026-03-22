package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "artist_profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArtistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "stage_name", length = 50, nullable = false)
    private String stageName;
    @Column(length = 500)
    private String bio;
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;
    @Column(name = "avatar_key", length = 150, nullable = false)
    private String avatarKey;
    @Column(name = "cover_key", length = 150, nullable = false)
    private String coverKey;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(
            mappedBy = "artist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Album> albums;

    @OneToMany(
            mappedBy = "contributor",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<ArtistContribution> contributions;

    @OneToMany(
            mappedBy = "artist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Follower> followers;
}
