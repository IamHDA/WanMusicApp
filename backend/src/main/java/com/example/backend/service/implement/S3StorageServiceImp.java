package com.example.backend.service.implement;

import com.example.backend.dto.PresignedUploadRequestDTO;
import com.example.backend.dto.PresignedUploadResponseDTO;
import com.example.backend.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageServiceImp implements S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private String checkFileExist(String fileKey, String bucketName){
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileKey)
                            .build()
            );
        }catch (Exception e){
            if(e.getMessage().contains("NoSuchKey")) return null;
            else throw e;
        }
        return "exists";
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName) throws IOException {
        String fileKey = file.getName().replace("\\s+", "_");
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        return fileKey;
    }

    @Override
    public String deleteFile(String fileKey, String bucketName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        return "File deleted successfully!";
    }

    @Override
    public String getGetPresignedUrl(String fileKey, String bucketName) {
        GetObjectRequest gor = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner
                .presignGetObject(b -> b
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(gor)
                );

        return presignedRequest.url().toString();
    }

    @Override
    public PresignedUploadResponseDTO getPutPresignedUrl(PresignedUploadRequestDTO request) {
        String key = UUID.randomUUID() + "_" + request.fileName().replace("\\s+", "").trim();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(request.bucketName())
                .key(key)
                .contentType(request.fileType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUploadResponseDTO(presignedRequest.url().toString(), key);
    }
}
