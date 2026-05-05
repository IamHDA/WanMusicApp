package com.example.backend.dto.jam;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackPreviewDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JamQueueRequestDTO {

    PageResponse<TrackPreviewDTO> tracks;
    int index;
    Long jamId;

}
