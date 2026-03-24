package com.example.backend.dto.album;

public record AlbumPreviewDTO(
        Long id,
        String title,
        String thumbnailUrl,
        int releaseYear
) {}
