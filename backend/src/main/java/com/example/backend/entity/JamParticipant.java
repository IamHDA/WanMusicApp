package com.example.backend.entity;

import com.example.backend.entity.EmbeddedId.JamParticipantId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jam_participant")
@Getter
@Setter
@NoArgsConstructor
public class JamParticipant {

    @EmbeddedId
    private JamParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private JamSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member participant;

    public JamParticipant(JamSession session, Member participant){
        this.session = session;
        this.participant = participant;
    }
}
