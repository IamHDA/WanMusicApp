package com.example.backend.dto;

public record PresignedUploadRequestDTO (
        String fileName,
        String fileType,
        String bucketName
) {}
