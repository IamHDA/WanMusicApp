package com.example.backend.dto;

public record CreateInteractionRequestDTO (
        String interactionType,
        int duration,
        Long trackId
){
}
