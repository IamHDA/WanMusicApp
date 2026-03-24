package com.example.backend.dto.track;

import com.example.backend.dto.TagDTO;
import com.example.backend.dto.user.UserPreviewDTO;

import java.util.List;

public record TrackDraftResponseDTO (
        Long trackId,
        String title,
        String trackUrl,
        String thumbnailUrl,
        List<TagDTO> recommendedTags,
        List<UserPreviewDTO> featuredArtists
){}
