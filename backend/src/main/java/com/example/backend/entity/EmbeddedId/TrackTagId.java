package com.example.backend.entity.EmbeddedId;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackTagId {
    private Long trackId;
    private Long tagId;
}
