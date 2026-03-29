package com.example.backend.controller;

import com.example.backend.dto.SharePlaylistRequestDTO;
import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.playlist.UpdatePlaylistDetailDTO;
import com.example.backend.service.PlaylistService;
import com.example.backend.service.SharedPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final SharedPlaylistService sharedPlaylistService;

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO> getPlaylist(@PathVariable Long id){
        return ResponseEntity.ok(playlistService.getPlaylistById(id));
    }

    @GetMapping("/getMemberPlaylists/{id}")
    public ResponseEntity<List<PlaylistPreviewDTO>> getMyPlaylists(@PathVariable Long id){
        return ResponseEntity.ok(playlistService.getPlaylistsByOwnerId(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Long> createPlaylist(@RequestBody Map<String, String> params){
        return ResponseEntity.ok(playlistService.createPlaylist(params.get("name")));
    }

    @PostMapping("/sharePlaylist")
    public ResponseEntity<String> sharePlaylist(@RequestBody SharePlaylistRequestDTO dto){
        return ResponseEntity.ok(sharedPlaylistService.sharePlaylist(dto));
    }

    @PutMapping("/updateDetail")
    public ResponseEntity<String> updatePlaylistDetail(@RequestBody UpdatePlaylistDetailDTO dto){
        return ResponseEntity.ok(playlistService.updatePlaylistDetail(dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePlaylist(@PathVariable Long id){
        return ResponseEntity.ok(playlistService.deletePlaylist(id));
    }
}
