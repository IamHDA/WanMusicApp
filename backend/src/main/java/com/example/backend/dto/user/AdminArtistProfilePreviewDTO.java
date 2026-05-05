package com.example.backend.dto.user;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminArtistProfilePreviewDTO {
    private Long id;
    private String stageName;
    private String avatarUrl;  // Sẽ map từ avatarKey qua S3
    private String status;
    private LocalDateTime createdAt;
}
