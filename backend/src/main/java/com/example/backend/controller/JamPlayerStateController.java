package com.example.backend.controller;

import com.example.backend.dto.jam.JamPlayerStateRequestDTO;
import com.example.backend.dto.jam.UpdateJamPlayerRequestDTO;
import com.example.backend.service.JamPlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jam-player-state")
@RequiredArgsConstructor
public class JamPlayerStateController {

    private final JamPlayerStateService jamPlayerStateService;

    @MessageMapping("/jam/player-state")
    public void handleJamPlayerState(JamPlayerStateRequestDTO dto){
        jamPlayerStateService.updateJamPlayerState(dto.getJamId(), dto.getCurrentSeekPosition(), dto.isPlaying(), dto.getPlaybackRate());
    }

    @PutMapping("/accept")
    public ResponseEntity<Void> acceptJamPlayerState(@RequestBody UpdateJamPlayerRequestDTO dto){
        jamPlayerStateService.acceptJamMemberRequest(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reject")
    public ResponseEntity<Void> rejectJamPlayerState(@RequestBody UpdateJamPlayerRequestDTO dto){
        jamPlayerStateService.rejectJamMemberRequest(dto);
        return ResponseEntity.ok().build();
    }

}
