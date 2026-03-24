package com.example.backend.dto;

public record CreateTagRequestDTO (
        String name,
        String description,
        Long parentTagId
) {
}
