package com.example.backend.controller;

import com.example.backend.dto.SavePlayerStateRequestDTO;
import com.example.backend.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player-state")
@RequiredArgsConstructor
public class PlayerStateController {

    private final PlayerStateService playerStateService;

    @PutMapping
    public ResponseEntity<String> savePlayerState(@RequestBody SavePlayerStateRequestDTO dto){
        return ResponseEntity.ok(playerStateService.savePlayerState(dto));
    }

}
