package com.example.backend.dto.user;

public record UpdateArtistProfileRequestDTO(
        String stageName,
        String bio,
        String avatarKey,
        String coverKey
) {}
