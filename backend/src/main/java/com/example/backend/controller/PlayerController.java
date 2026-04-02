package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/queue")
    public ResponseEntity<PageResponse<TrackPreviewDTO>> getPlayerQueue(@RequestParam int index){
        return ResponseEntity.ok(playerService.generateQueue(index));
    }

}
