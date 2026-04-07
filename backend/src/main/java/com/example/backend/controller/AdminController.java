package com.example.backend.controller;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.CreateTagRequestDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.TagDTO;
import com.example.backend.dto.track.TrackAdminReviewDTO;
import com.example.backend.dto.user.AdminArtistProfileDTO;
import com.example.backend.dto.user.AdminArtistProfilePreviewDTO;
import com.example.backend.service.AdminService;
import com.example.backend.service.ArtistProfileService;
import com.example.backend.service.TagService;
import com.example.backend.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TrackService trackService;
    private final AdminService adminService;

    @GetMapping("/getAllPendingTrack")
    public ResponseEntity<PageResponse<TrackAdminReviewDTO>> getAllPendingTracks(@RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "6") int size){
        return ResponseEntity.ok(trackService.getTracksByStatus(TrackStatus.PENDING, index - 1, size));
    }

    @GetMapping("/getAllPendingArtistProfile")
    public ResponseEntity<List<AdminArtistProfilePreviewDTO>> getAllPendingArtistProfiles() {
        return ResponseEntity.ok(adminService.getAllPendingArtistProfile());
    }

    @GetMapping("/getArtistProfile/{id}")
    public ResponseEntity<AdminArtistProfileDTO> getArtistProfileDetail(@PathVariable Long id){
        return ResponseEntity.ok(adminService.getArtistProfileDetail(id));
    }

    @PutMapping("/approveArtistProfile/{id}")
    public ResponseEntity<String> approveArtistProfileRequest(@PathVariable Long id){
        return ResponseEntity.ok(adminService.approveArtistProfileRequest(id));
    }

    @PutMapping("/rejectArtistProfile/{id}")
    public ResponseEntity<String> rejectArtistProfileRequest(@PathVariable Long id){
        return ResponseEntity.ok(adminService.rejectArtistProfileRequest(id));
    }

    @PutMapping("/approveTrack/{id}")
    public ResponseEntity<String> approveTrack(@PathVariable Long id){
        return ResponseEntity.ok(adminService.approveTrackRequest(id));
    }

    @PutMapping("/rejectTrack/{id}")
    public ResponseEntity<String> rejectTrack(@PathVariable Long id){
        return ResponseEntity.ok(adminService.rejectTrackRequest(id));
    }

    @PutMapping("/approveAlbum/{id}")
    public ResponseEntity<String> approveAlbum(@PathVariable Long id){
        return ResponseEntity.ok(adminService.approveAlbumRequest(id));
    }

    @PutMapping("/rejectAlbum/{id}")
    public ResponseEntity<String> rejectAlbum(@PathVariable Long id){
        return ResponseEntity.ok(adminService.rejectAlbumRequest(id));
    }

}
