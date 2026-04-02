package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jam_session")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "session_code", unique = true, nullable = false)
    private String sessionCode;
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;
    @Column(nullable = false)
    private int size;

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "owner_id")
    private Member owner;

    @OneToMany(
            mappedBy = "session",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JamParticipant> participants;


    @OneToMany(
            mappedBy = "jamSession",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JamNotification> notifications;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "current_track_id")
    private Track currentTrack;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "context_playlist_id")
    private Playlist contextPlaylist;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "context_album_id")
    private Album contextAlbum;
}
