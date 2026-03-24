package com.example.backend.dto.track;


import java.util.Set;

public record TrackSubmitDTO (
        Long id,
        Set<Long> artistIds,
        Set<Long> tagIds
){}
