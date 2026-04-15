package com.example.backend.dto.album;

import com.example.backend.dto.user.ArtistProfilePreviewDTO;

public record AlbumPreviewDTO(
        Long id,
        String title,
        ArtistProfilePreviewDTO artist,
        String thumbnailUrl,
        int releaseYear
) {}
