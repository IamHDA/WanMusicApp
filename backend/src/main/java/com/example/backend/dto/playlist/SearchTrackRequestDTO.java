package com.example.backend.dto.playlist;

import java.util.List;

public record SearchTrackRequestDTO (
        List<Long> trackIds,
        String keyword,
        int pageNumber,
        int pageSize
) {
}
