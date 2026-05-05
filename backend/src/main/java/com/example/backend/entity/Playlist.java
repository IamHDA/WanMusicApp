package com.example.backend.entity;

import com.fasterxml.jackson.databind.annotation.JsonValueInstantiator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String title;
    @Column(name = "thumbnail_key", length = 500, nullable = false)
    private String thumbnailKey;
    @Column(length = 200)
    private String description;
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @OneToMany(
            mappedBy = "playlist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlaylistTrack> tracks;

    @OneToMany(
            mappedBy = "playlist",
            fetch = FetchType.LAZY
    )
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(
            mappedBy = "playlist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlaylistCollaborator> collaborators;

    @OneToMany(
            mappedBy = "playlist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SharedPlaylist> sharedPlaylists;

    @OneToMany(
            mappedBy = "contextPlaylist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JamSession> jamSessions;

    @OneToMany(
            mappedBy = "playlist",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlayerState> playerStates;
}
