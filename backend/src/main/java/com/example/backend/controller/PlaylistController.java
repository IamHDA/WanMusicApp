package com.example.backend.controller;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getPlaylist(@PathVariable Long id){
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @GetMapping("/getMyPlaylists/{id}")

    @PostMapping("/create")
    public ResponseEntity<Long> createPlaylist(@RequestBody Map<String, String> params){
        return ResponseEntity.ok(playlistService.createPlaylist(params.get("name")));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePlaylist(@PathVariable Long id){
        return ResponseEntity.ok(playlistService.deletePlaylist(id));
    }
}
