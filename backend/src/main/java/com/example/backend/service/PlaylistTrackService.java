package com.example.backend.service;

import com.example.backend.dto.playlistTrack.PlaylistTrackDTO;
import com.example.backend.dto.track.TrackPreviewDTO;

import java.util.List;

public interface PlaylistTrackService {
    String addTrackToPlaylist(PlaylistTrackDTO dto);
    String removeTrackFromPlaylist(PlaylistTrackDTO dto);
    List<TrackPreviewDTO> getPlaylistTracks(Long playlistId);
}
