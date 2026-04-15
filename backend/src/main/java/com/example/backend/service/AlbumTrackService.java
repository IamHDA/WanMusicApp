package com.example.backend.service;

import com.example.backend.dto.album.AddTrackToAlbumRequestDTO;
import com.example.backend.dto.track.TrackDTO;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;

import java.util.List;

public interface AlbumTrackService {
    AlbumTrackId addTrackToAlbum(AddTrackToAlbumRequestDTO dto);
    List<TrackDTO> getTracksInAlbum(Long albumId);
    String removeTrackFromAlbum(Long albumId, Long trackId);
}
