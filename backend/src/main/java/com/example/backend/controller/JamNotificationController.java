package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.jam.GetJamNotificationRequestDTO;
import com.example.backend.dto.jam.JamNotificationDTO;
import com.example.backend.service.JamNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jam-notification")
public class JamNotificationController {

    private final JamNotificationService jamNotificationService;

    @GetMapping
    private ResponseEntity<PageResponse<JamNotificationDTO>> getNotifications(
            @RequestParam(name = "jamId") Long jamId,
            @RequestParam(name = "pageNumber") int index,
            @RequestParam(name = "pageSize") int size
    ){
        GetJamNotificationRequestDTO dto = new GetJamNotificationRequestDTO(jamId, index, size);
        return ResponseEntity.ok(jamNotificationService.getJamNotifications(dto));
    }

}
