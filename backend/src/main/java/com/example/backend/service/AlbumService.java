package com.example.backend.service;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.album.CreateAlbumDraftRequestDTO;
import com.example.backend.dto.album.GetAlbumsPaginationRequest;

public interface AlbumService {
    PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(GetAlbumsPaginationRequest request);
    Long createAlbumDraft(CreateAlbumDraftRequestDTO dto);
    String submitAlbum(Long albumId);

    PageResponse<AlbumPreviewDTO> getAllAlbums(GetAlbumsPaginationRequest request);
}
