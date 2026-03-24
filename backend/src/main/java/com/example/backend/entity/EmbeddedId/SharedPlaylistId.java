package com.example.backend.entity.EmbeddedId;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharedPlaylistId {
    private Long memberId;
    private Long playlistId;
}
