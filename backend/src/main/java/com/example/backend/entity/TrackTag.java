package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.TrackTagId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "track_tag")
@NoArgsConstructor
@AllArgsConstructor
public class TrackTag {

    @EmbeddedId
    private TrackTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public TrackTag(Track track, Tag tag){
        this.track = track;
        this.tag = tag;
        this.id = new TrackTagId(track.getId(), tag.getId());
    }
}
