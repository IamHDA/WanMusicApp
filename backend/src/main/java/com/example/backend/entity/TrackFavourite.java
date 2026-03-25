package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.TrackFavouriteId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "track_favourite")
public class TrackFavourite {

    @EmbeddedId
    private TrackFavouriteId id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "trackId")
    private Track track;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TrackFavourite(Member member, Track track){
        this.id = new TrackFavouriteId(member.getId(), track.getId());
        this.member = member;
        this.track = track;
        this.createdAt = LocalDateTime.now();
    }

}
