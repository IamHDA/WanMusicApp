package com.example.backend.dto.user;

public record UserPreviewDTO(
        Long id,
        String avatarUrl,
        String name,
        boolean isFollowed,
        boolean isFriend
){}
