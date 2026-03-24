package com.example.backend.dto.track;

import java.util.Set;

public record TrackCreateDraftDTO (
        String title,
        String trackKey,
        String thumbnailKey,
        int duration,
        Set<Long> tagIds,
        Set<Long> featuredArtistIds
){}
