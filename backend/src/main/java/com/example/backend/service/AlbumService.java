package com.example.backend.service;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.album.CreateAlbumRequestDTO;

public interface AlbumService {
    PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(Long artistId);
    String createAlbumRequest(CreateAlbumRequestDTO dto);

}
