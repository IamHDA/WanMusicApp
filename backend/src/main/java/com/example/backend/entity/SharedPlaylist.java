package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.SharedPlaylistId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "shared_playlist")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharedPlaylist {

    @EmbeddedId
    private SharedPlaylistId id;
    @Column(name = "saved_at", nullable = false)
    private LocalDate savedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_Id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_Id")
    private Member sharedMember;

    public SharedPlaylist(Playlist playlist, Member member){
        this.playlist = playlist;
        this.sharedMember = member;
        this.savedAt = LocalDate.now();
    }
}
