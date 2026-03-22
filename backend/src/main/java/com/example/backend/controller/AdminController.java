package com.example.backend.controller;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackAdminReviewDTO;
import com.example.backend.service.ArtistProfileService;
import com.example.backend.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TrackService trackService;
    private final ArtistProfileService artistProfileService;

    @GetMapping("/getAllPendingTrack")
    public ResponseEntity<PageResponse<TrackAdminReviewDTO>> getAllPendingTracks(@RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "6") int size){
        return ResponseEntity.ok(trackService.getTracksByStatus(TrackStatus.PENDING, index - 1, size));
    }

    @PutMapping("/approveArtistProfile")
    public ResponseEntity<String> approveArtistProfileRequest(@RequestBody Map<String, Long> param){
        return ResponseEntity.ok(artistProfileService.approveArtistProfileRequest(param.get("artistProfileId")));
    }

    @PutMapping("/rejectArtistProfile")
    public ResponseEntity<String> rejectArtistProfileRequest(@RequestBody Map<String, Long> param){
        return ResponseEntity.ok(artistProfileService.rejectArtistProfileRequest(param.get("artistProfileId")));
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
