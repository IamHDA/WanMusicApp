package com.example.backend.dto;

public record CreateTagRequestDTO (
        String name,
        String displayName,
        String description,
        Long parentTagId
) {
}
