package com.example.backend.entity;

import com.example.backend.Enum.TrackStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String title;
    @Column(name = "thumbnail_key",length = 500, nullable = false)
    private String thumbnailKey;
    @Column(name = "file_key", length = 500, nullable = false)
    private String fileKey;
    @Column(nullable = false)
    private int duration;
    @Column(name = "is_explicit", nullable = false)
    private boolean isExplicit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TrackTag> tags = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY
    )
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlaylistTrack> playlists = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AlbumTrack> albums = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ArtistContribution> contributions = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserInteraction> interactions = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlayerState> playerStates = new ArrayList<>();

    @OneToMany(
            mappedBy = "currentTrack",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JamSession> jamSessions = new ArrayList<>();

    @OneToMany(
            mappedBy = "track",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TrackFavourite> favourites = new ArrayList<>();
}
