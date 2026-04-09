package com.example.backend.dto.album;

public record GetAlbumsPaginationRequest(
        Long artistId,
        int index,
        int size
) {
}
