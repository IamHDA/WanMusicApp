package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "jam_player_state")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JamPlayerState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_seek_position", nullable = false)
    private int currentSeekPosition;

    @Column(name = "is_playing", nullable = false)
    private boolean isPlaying;

    @Column(name = "playback_rate", nullable = false)
    private float playbackRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "jam_session_id",
            nullable = false,
            unique = true
    )
    private JamSession jamSession;
}
