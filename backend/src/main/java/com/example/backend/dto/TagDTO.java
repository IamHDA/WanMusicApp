package com.example.backend.dto;

public record TagDTO (
        Long id,
        String name,
        String displayName,
        String description,
        Long parentTagId
) {}
