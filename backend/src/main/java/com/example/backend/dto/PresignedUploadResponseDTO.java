package com.example.backend.dto;

public record PresignedUploadResponseDTO (
        String url,
        String key
){}
