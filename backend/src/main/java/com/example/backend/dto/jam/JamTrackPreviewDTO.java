package com.example.backend.dto.jam;

import com.example.backend.dto.track.TrackPreviewDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JamTrackPreviewDTO extends TrackPreviewDTO {
    private int currentSeekPosition;
    private float playbackRate;
    private boolean isPlaying;

    public JamTrackPreviewDTO(TrackPreviewDTO trackPreviewDTO) {
        this.setId(trackPreviewDTO.getId());
        this.setTrackUrl(trackPreviewDTO.getTrackUrl());
        this.setDuration(trackPreviewDTO.getDuration());
        this.setThumbnailUrl(trackPreviewDTO.getThumbnailUrl());
        this.setTitle(trackPreviewDTO.getTitle());
        this.setContributors(trackPreviewDTO.getContributors());
        this.currentSeekPosition = 0;
        this.isPlaying = true;
    }
}
