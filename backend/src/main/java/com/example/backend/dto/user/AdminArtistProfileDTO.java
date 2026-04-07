package com.example.backend.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminArtistProfileDTO {
    private Long id;
    private String stageName;
    private String bio;
    private String avatarUrl; // Sẽ map từ avatarKey
    private String coverUrl;  // Sẽ map từ coverKey
    private String status;
    private LocalDateTime createdAt;
}
