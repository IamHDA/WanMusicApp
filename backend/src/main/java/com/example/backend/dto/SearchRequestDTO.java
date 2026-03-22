package com.example.backend.dto;

import lombok.Builder;

@Builder
public record SearchRequestDTO(
        //Search Fields
        String keyword,
        String type,
        int pageSize,
        int pageNumber
) {
}
