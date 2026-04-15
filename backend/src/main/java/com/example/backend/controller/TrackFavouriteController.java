package com.example.backend.controller;

import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.service.TrackFavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/track-favourite")
@RequiredArgsConstructor
public class TrackFavouriteController {

    private final TrackFavouriteService trackFavouriteService;

    @GetMapping
    public ResponseEntity<List<TrackPreviewDTO>> isTrackFavourite(@RequestParam boolean updated){
        return ResponseEntity.ok(trackFavouriteService.getFavouriteTracks(updated) );
    }

    @PostMapping
    public ResponseEntity<String> addTrackToFavourite(@RequestBody Map<String, String> request){
        Long trackId = Long.parseLong(request.get("trackId"));
        return ResponseEntity.ok(trackFavouriteService.addTrackToFavourite(trackId));
    }

    @DeleteMapping
    public ResponseEntity<String> removeTrackFromFavourite(@RequestBody Map<String, String> request){
        Long trackId = Long.parseLong(request.get("trackId"));
        return ResponseEntity.ok(trackFavouriteService.removeTrackFromFavourite(trackId));
    }

}
