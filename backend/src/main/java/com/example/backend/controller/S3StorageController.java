package com.example.backend.controller;

import com.example.backend.dto.PresignedUploadRequestDTO;
import com.example.backend.dto.PresignedUploadResponseDTO;
import com.example.backend.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/storage")
@RequiredArgsConstructor
public class S3StorageController {

    private final S3StorageService s3StorageService;

    @PostMapping("/upload")
    public ResponseEntity<PresignedUploadResponseDTO> uploadFile(@RequestBody PresignedUploadRequestDTO request){
        return ResponseEntity.ok(s3StorageService.getPutPresignedUrl(request));
    }
}
