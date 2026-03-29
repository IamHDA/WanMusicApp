package com.example.backend.service;

public interface TrackFavouriteService {
    String addTrackToFavourite(Long trackId);
    String removeTrackFromFavourite(Long trackId);
}
