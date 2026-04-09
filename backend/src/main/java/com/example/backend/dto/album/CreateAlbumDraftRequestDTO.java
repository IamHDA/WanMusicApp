package com.example.backend.dto.album;

public record CreateAlbumDraftRequestDTO(
        String title,
        String thumbnailKey,
        int releaseDate
) {
}
