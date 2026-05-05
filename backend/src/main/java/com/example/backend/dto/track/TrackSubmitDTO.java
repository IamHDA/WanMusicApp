package com.example.backend.dto.track;


import com.example.backend.dto.ContributorDTO;

import java.util.Set;

public record TrackSubmitDTO (
        Long id,
        Set<ContributorDTO> contributors,
        Set<Long> tagIds
){}
