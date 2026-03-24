package com.example.backend.dto.track;

public record TrackDTO (
        Long id,
        String title,
        String trackUrl,
        String thumbnailUrl,
        int duration
) {}
