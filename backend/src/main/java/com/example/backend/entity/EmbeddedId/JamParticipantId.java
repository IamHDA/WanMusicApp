package com.example.backend.entity.EmbeddedId;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JamParticipantId {
    private Long sessionId;
    private Long memberId;
}
