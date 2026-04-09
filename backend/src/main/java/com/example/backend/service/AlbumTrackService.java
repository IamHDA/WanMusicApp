package com.example.backend.service;

import com.example.backend.dto.album.AddTrackToAlbumRequestDTO;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;

public interface AlbumTrackService {
    AlbumTrackId addTrackToAlbum(AddTrackToAlbumRequestDTO dto);
    String removeTrackFromAlbum(Long albumId, Long trackId);
}
