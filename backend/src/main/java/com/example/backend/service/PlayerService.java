package com.example.backend.service;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackPreviewDTO;

public interface PlayerService {

    PageResponse<TrackPreviewDTO> generateQueue(int index);

}
