package com.example.backend.dto;

public record SharePlaylistRequestDTO(
        Long playlistId,
        Long sharedMemberId
) {
}
