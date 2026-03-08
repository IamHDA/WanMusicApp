package com.example.backend.controller;

import com.example.backend.dto.track.TrackCreateDraftDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.dto.track.TrackSubmitDTO;
import com.example.backend.service.TrackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/track")
public class TrackController {

    private final TrackService trackService;

    @PostMapping("/submitDraft")
    public ResponseEntity<TrackDraftResponseDTO> submitDraft(@RequestBody TrackCreateDraftDTO dto){
        return ResponseEntity.ok(trackService.createDraft(dto));
    }

    @PostMapping("/submitFinalize")
    public ResponseEntity<String> submitFinalize(@RequestBody TrackSubmitDTO dto){
        return ResponseEntity.ok(trackService.submitTrack(dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrack(@PathVariable Long id){
        return ResponseEntity.ok(trackService.deleteTrack(id));
    }
}
