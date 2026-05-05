package com.example.backend.dto.track;

import java.util.List;

public record TrackFinalizeDTO (
        Long trackId,
        List<Long> tagIds,
        List<Long> featuredArtistIds
){}
