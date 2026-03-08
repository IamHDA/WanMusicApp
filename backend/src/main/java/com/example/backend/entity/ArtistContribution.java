package com.example.backend.entity;

import com.example.backend.Enum.ContributorRole;
import com.example.backend.entity.EmbeddedId.ArtistContributionId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "artist_contribution")
@Getter
@Setter
@NoArgsConstructor
public class ArtistContribution {

    @EmbeddedId
    private ArtistContributionId id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private ArtistProfile contributor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributorRole role;

    public ArtistContribution(Track track, ArtistProfile contributor){
        this.track = track;
        this.contributor = contributor;

    }
}
