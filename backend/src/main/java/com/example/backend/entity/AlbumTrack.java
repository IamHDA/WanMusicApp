package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.AlbumTrackId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "album_track")
@Getter
@Setter
@NoArgsConstructor
public class AlbumTrack {

    @EmbeddedId
    private AlbumTrackId id;
    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("albumId")
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    public AlbumTrack(Album album, Track track, int position){
        this.album = album;
        this.track = track;
        this.position = position;
    }
}
