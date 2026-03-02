package com.example.backend.controller;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackReviewDTO;
import com.example.backend.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TrackService trackService;

    @GetMapping("/getAllPendingTrack")
    public ResponseEntity<PageResponse<TrackReviewDTO>> getAllPendingTracks(@RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "6") int size){
        return ResponseEntity.ok(trackService.getTracksByStatus(TrackStatus.PENDING, index - 1, size));
    }

    @PutMapping("/approveTrack/{id}")
    public ResponseEntity<String> approveTrack(@PathVariable Long id){
        return ResponseEntity.ok(trackService.approveTrack(id));
    }

    @PutMapping("/rejectTrack/{id}")
    public ResponseEntity<String> rejectTrack(@PathVariable Long id){
        return ResponseEntity.ok(trackService.rejectTrack(id));
    }

}
