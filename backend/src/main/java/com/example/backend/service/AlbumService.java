package com.example.backend.service;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;

public interface AlbumService {
    PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(Long artistId);
}
