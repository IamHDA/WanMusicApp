package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.FollowerId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Follower {

    @EmbeddedId
    private FollowerId id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistProfile artist;

    public Follower(Member follower, ArtistProfile artist){
        this.follower = follower;
        this.artist = artist;
    }
}
