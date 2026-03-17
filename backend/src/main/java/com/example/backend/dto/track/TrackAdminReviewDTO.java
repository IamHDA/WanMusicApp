package com.example.backend.dto.track;

import com.example.backend.dto.TagDTO;

import java.util.List;

public record TrackAdminReviewDTO(
        Long trackId,
        String title,
        int duration,
        String trackUrl,
        String thumbnailUrl,
        List<TagDTO> tags
) {}
