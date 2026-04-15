package com.example.backend.dto.jam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JamPlayerStateRequestDTO {

    private Long jamId;
    private int currentSeekPosition;
    private float playbackRate;

    @JsonProperty("isPlaying")
    private boolean isPlaying;

}
