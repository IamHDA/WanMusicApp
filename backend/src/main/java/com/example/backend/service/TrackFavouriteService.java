package com.example.backend.service;

import com.example.backend.dto.track.TrackPreviewDTO;

import java.util.List;

public interface TrackFavouriteService {
    List<TrackPreviewDTO> getFavouriteTracks(boolean updated);
    String addTrackToFavourite(Long trackId);
    String removeTrackFromFavourite(Long trackId);
}
