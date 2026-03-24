package com.example.backend.entity.EmbeddedId;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ArtistContributionId {
    private Long trackId;
    private Long artistId;
}
