package com.example.backend.dto;

import lombok.Builder;

@Builder
public record SearchRequestDTO(
        //Search Fields
        String keyword,
        Integer trackId,
        String type,
        int pageSize,
        int pageNumber
) {
}
