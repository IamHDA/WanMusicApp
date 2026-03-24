package com.example.backend.controller;

import com.example.backend.dto.jam.AcceptInvitationRequestDTO;
import com.example.backend.dto.jam.CreateJamSessionRequestDTO;
import com.example.backend.service.JamParticipantService;
import com.example.backend.service.JamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jam")
@RequiredArgsConstructor
public class JamController {

    private final JamSessionService jamSessionService;
    private final JamParticipantService jamParticipantService;

    @PostMapping
    public ResponseEntity<String> createJamSession(@RequestBody CreateJamSessionRequestDTO dto){
        return ResponseEntity.ok(jamSessionService.createJamSession(dto));
    }

    @PutMapping("/join")
    public ResponseEntity<String> joinJamSession(@RequestBody AcceptInvitationRequestDTO dto){
        return ResponseEntity.ok(jamParticipantService.joinJam(dto));
    }
}
