package com.example.backend.dto.user;

public record CreateArtistProfileRequestDTO (
        String stageName,
        String avatarKey,
        String coverKey,
        String bio
) {
}
