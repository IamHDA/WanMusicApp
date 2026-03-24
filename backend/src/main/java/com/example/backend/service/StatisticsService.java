package com.example.backend.service;

import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackStatisticDTO;

import java.util.List;

public interface StatisticsService {

    TrackStatisticDTO getTrackStatistic(Long trackId);
    List<TrackPreviewDTO> getTopTracks();

}
