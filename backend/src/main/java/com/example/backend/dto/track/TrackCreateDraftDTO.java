package com.example.backend.dto.track;

import com.example.backend.dto.ContributorDTO;

import java.util.Set;

public record TrackCreateDraftDTO (
        String title,
        String trackKey,
        String thumbnailKey,
        int duration,
        Set<ContributorDTO> featuredArtistDTO
){}
