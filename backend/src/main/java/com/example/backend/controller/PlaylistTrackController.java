package com.example.backend.controller;

import com.example.backend.dto.playlistTrack.PlaylistTrackDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.service.PlaylistTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/playlist-track")
@RequiredArgsConstructor
public class PlaylistTrackController {

    private final PlaylistTrackService playlistTrackService;

    @GetMapping("/{playlistId}")
    public ResponseEntity<List<TrackPreviewDTO>> getTracksByPlaylistId(@PathVariable Long playlistId){
        return ResponseEntity.ok(playlistTrackService.getPlaylistTracks(playlistId));
    }

    @PostMapping("/addTrack")
    public ResponseEntity<String> addTrackToPlaylist(@RequestBody PlaylistTrackDTO request){
        return ResponseEntity.ok(playlistTrackService.addTrackToPlaylist(request));
    }

    @DeleteMapping("/removeTrack")
    public ResponseEntity<String> removeTrackToPlaylist(@RequestBody PlaylistTrackDTO request){
        return ResponseEntity.ok(playlistTrackService.removeTrackFromPlaylist(request));
    }

}
