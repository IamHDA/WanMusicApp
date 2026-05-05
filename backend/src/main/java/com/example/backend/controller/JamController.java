package com.example.backend.controller;

import com.example.backend.dto.CreateJamInvitationRequestDTO;
import com.example.backend.dto.jam.*;
import com.example.backend.service.JamParticipantService;
import com.example.backend.service.JamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jam")
@RequiredArgsConstructor
public class JamController {

    private final JamSessionService jamSessionService;
    private final JamParticipantService jamParticipantService;

    @GetMapping("/{id}")
    public ResponseEntity<JamDTO> getJamSessionById(@PathVariable Long id){
        return ResponseEntity.ok(jamSessionService.getJamSessionById(id));
    }

    @PostMapping
    public ResponseEntity<JamPreviewDTO> createJamSession(@RequestBody CreateJamSessionRequestDTO dto){
        return ResponseEntity.ok(jamSessionService.createJamSession(dto));
    }

    @PostMapping("/invite")
    public ResponseEntity<String> inviteToJamSession(@RequestBody CreateJamInvitationRequestDTO dto){
        return ResponseEntity.ok(jamParticipantService.inviteMember(dto));
    }

    @PutMapping("/joinById")
    public ResponseEntity<Long> joinJamSessionById(@RequestBody JamParticipantRequestDTO dto){
        return ResponseEntity.ok(jamParticipantService.joinJamById(dto));
    }

    @PutMapping("/joinByCode")
    public ResponseEntity<Long> joinJamSessionByCode(@RequestBody JamParticipantRequestDTO dto){
        return ResponseEntity.ok(jamParticipantService.joinJamByCode(dto));
    }

    @PutMapping("/context")
    public ResponseEntity<Void> updateJamSessionContext(@RequestBody UpdateJamSessionContextRequestDTO dto){
        jamSessionService.updateJamSessionContext(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/leave")
    public ResponseEntity<String> leaveJamSession(@RequestBody JamParticipantRequestDTO dto){
        return ResponseEntity.ok(jamParticipantService.leaveJam(dto));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateJamSession(@RequestBody UpdateJamSessionRequestDTO dto){
        return ResponseEntity.ok(jamSessionService.updateJamSession(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> leaveJamSession(@PathVariable Long id){
        return ResponseEntity.ok(jamSessionService.deleteJamSession(id));
    }

}
