package com.example.backend.dto.jam;

public record JamSessionTrackRequest(
        Long jamId,
        Long trackId
) {
}
