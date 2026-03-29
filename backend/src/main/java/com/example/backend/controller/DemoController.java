package com.example.backend.controller;

import com.example.backend.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final S3StorageService s3StorageService;

    @GetMapping("/getTracks")
    public ResponseEntity<List<String>> getTracks(){
        List<String> tracks = List.of("dau-nhat-la-lang-im", "van-su-nhu-y", "co-ai-hen-ho-cung-em-chua");
        List<String> urls = new ArrayList<>();

        for(String track : tracks) {
            urls.add(s3StorageService.getGetPresignedUrl(track, "songs"));
        }

        return ResponseEntity.ok(urls);
    }

}
