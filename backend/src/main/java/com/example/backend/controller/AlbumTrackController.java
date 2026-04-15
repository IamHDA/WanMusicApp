package com.example.backend.controller;

import com.example.backend.dto.album.AddTrackToAlbumRequestDTO;
import com.example.backend.dto.track.TrackDTO;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;
import com.example.backend.service.AlbumTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album-track")
@RequiredArgsConstructor
public class AlbumTrackController {

    private final AlbumTrackService albumTrackService;

    @PostMapping
    public ResponseEntity<AlbumTrackId> addTrackToAlbum(@RequestBody AddTrackToAlbumRequestDTO dto){
        return ResponseEntity.ok(albumTrackService.addTrackToAlbum(dto));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<List<TrackDTO>> getTrackByAlbumId(@PathVariable Long albumId){
        return ResponseEntity.ok(albumTrackService.getTracksInAlbum(albumId));
    }

    @DeleteMapping("/{albumId}/{trackId}")
    public ResponseEntity<String> removeTrackFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long trackId
    ){
        return ResponseEntity.ok(albumTrackService.removeTrackFromAlbum(albumId, trackId));
    }

}
