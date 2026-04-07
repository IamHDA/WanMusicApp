package com.example.backend.service;

import com.example.backend.dto.PresignedUploadRequestDTO;
import com.example.backend.dto.PresignedUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface S3StorageService {
    InputStream getFile(String fileKey, String bucketName);
    String uploadFile(MultipartFile file, String bucketName) throws IOException;
    void deleteFile(String fileKey, String bucketName);
    String getGetPresignedUrl(String fileKey, String bucketName);
    PresignedUploadResponseDTO getPutPresignedUrl(PresignedUploadRequestDTO request);
}
