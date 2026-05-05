package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.jam.JamPlayerStateRequestDTO;
import com.example.backend.dto.jam.JamQueueRequestDTO;
import com.example.backend.dto.jam.UpdateJamPlayerRequestDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.service.JamPlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jam-player-state")
@RequiredArgsConstructor
public class JamPlayerStateController {

    private final JamPlayerStateService jamPlayerStateService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/jam/player-state")
    public void handleJamPlayerState(JamPlayerStateRequestDTO dto){
        jamPlayerStateService.updateJamPlayerState(dto.getJamId(), dto.getCurrentSeekPosition(), dto.isPlaying());
    }

    @MessageMapping("/jam/player-state/queue")
    public void handleJamPlayerStateQueue(JamQueueRequestDTO dto){
        simpMessagingTemplate.convertAndSend("/jam/player-state/queue/" + dto.getJamId(), dto.getTracks());
    }

    @MessageMapping("/jam/player-state/queue/index")
    public void handleJamPlayerStateIndex(JamQueueRequestDTO dto){
        simpMessagingTemplate.convertAndSend("/jam/player-state/queue/index/" + dto.getJamId(), dto.getIndex());
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
