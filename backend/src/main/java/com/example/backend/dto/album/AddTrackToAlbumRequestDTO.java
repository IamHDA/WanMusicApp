package com.example.backend.dto.album;

public record AddTrackToAlbumRequestDTO (
        Long albumId,
        Long trackId,
        int position
) {
}
