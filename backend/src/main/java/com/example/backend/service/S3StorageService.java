package com.example.backend.service;

import com.example.backend.dto.PresignedUploadRequestDTO;
import com.example.backend.dto.PresignedUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3StorageService {
    String uploadFile(MultipartFile file, String bucketName) throws IOException;
    void deleteFile(String fileKey, String bucketName);
    String getGetPresignedUrl(String fileKey, String bucketName);
    PresignedUploadResponseDTO getPutPresignedUrl(PresignedUploadRequestDTO request);
}
