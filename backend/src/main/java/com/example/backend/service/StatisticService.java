package com.example.backend.service;

import com.example.backend.dto.AddInteractionRequestDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackStatisticDTO;
import com.example.backend.entity.UserInteraction;

import java.util.List;

public interface StatisticService {

    String addTrackStatistic(AddInteractionRequestDTO input);
    void addTrackStatistic(UserInteraction input);
    TrackStatisticDTO getTrackStatistic(Long trackId);
    List<TrackPreviewDTO> getTopTracks();

}
