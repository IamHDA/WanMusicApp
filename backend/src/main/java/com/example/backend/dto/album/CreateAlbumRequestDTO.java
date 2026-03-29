package com.example.backend.dto.album;

import java.util.List;
import java.util.Map;

public record CreateAlbumRequestDTO(
        String title,
        String thumbnailKey,
        int releaseDate,
        List<Long> trackIds,
        Map<Long, Integer> tracksIdAndPosition
) {
}
