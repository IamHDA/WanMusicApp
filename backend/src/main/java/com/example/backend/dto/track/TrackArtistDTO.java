package com.example.backend.dto.track;

public record TrackArtistDTO(
        Long id,
        String title,
        String thumbnailUrl,
        int duration,
        String status
){}
