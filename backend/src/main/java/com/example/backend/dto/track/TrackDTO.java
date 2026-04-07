package com.example.backend.dto.track;

import com.example.backend.dto.ContributorDTO;

import java.util.List;

public record TrackDTO (
        Long id,
        String title,
        String trackUrl,
        String thumbnailUrl,
        int duration,
        List<ContributorDTO> contributors
) {}
