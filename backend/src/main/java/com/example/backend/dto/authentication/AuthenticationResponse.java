package com.example.backend.dto.authentication;

public record AuthenticationResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        String message
) {}
