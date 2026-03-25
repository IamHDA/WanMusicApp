package com.example.backend.dto.playlist;

public record UpdatePlaylistDetailDTO(
        Long id,
        String name,
        String description,
        String thumbnailKey,
        boolean isPublic
) {
}
