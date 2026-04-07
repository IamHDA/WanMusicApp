package com.example.backend.dto.track;

import com.example.backend.dto.ContributorDTO;

import java.util.List;

public record TrackPreviewDTO(
        Long id,
        String title,
        String thumbnailUrl,
        String trackUrl,
        int duration,
        List<ContributorDTO> contributors
){
}
