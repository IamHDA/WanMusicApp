package com.example.backend.entity;

import com.example.backend.Enum.SubscriptionType;
import com.example.backend.Enum.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends User{
    @Column(name = "full_name", length = 50, nullable = false)
    private String fullName;
    @Column(name = "avatar_key", length = 150, nullable = false)
    private String avatarKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "subscriptionType", nullable = false)
    private SubscriptionType subscriptionType;

    @OneToMany(
            mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Friendship> members;

    @OneToMany(
            mappedBy = "friend",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Friendship> friends;

    @OneToMany(
            mappedBy = "collaborator",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<PlaylistCollaborator> collabPlaylists;

    @OneToMany(
            mappedBy = "owner",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<Playlist> playlists;

    @OneToMany(
            mappedBy = "addedBy",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<PlaylistTrack> addedTracks;

    @OneToMany(
            mappedBy = "sharedMember",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SharedPlaylist> sharedPlaylists;

    @OneToMany(
            mappedBy = "participant",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JamParticipant> participants;

    @OneToMany(
            mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<UserInteraction> interactions;

    @OneToMany(
            mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<UserTagPreference> userTagPreferences;

    @OneToMany(
            mappedBy = "subscriber",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<Subscription> subscriptions;

    @OneToMany(
            mappedBy = "receiver",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<Notification> notifications;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "jam_notification_id")
    private JamNotification jamNotification;

    @OneToMany(
            mappedBy = "follower",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Follower> artistsFollowed;
}
