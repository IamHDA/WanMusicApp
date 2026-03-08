package com.example.backend.dto.track;

import com.example.backend.dto.user.TrackContributorDTO;

import java.util.List;

public record TrackPreviewDTO(
        Long id,
        String title,
        String thumbnailUrl,
        int duration,
        List<TrackContributorDTO> contributors
){
}
