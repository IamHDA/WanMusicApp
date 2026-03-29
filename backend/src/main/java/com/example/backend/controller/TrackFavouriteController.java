package com.example.backend.controller;

import com.example.backend.service.TrackFavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/track-favourite")
@RequiredArgsConstructor
public class TrackFavouriteController {

    private final TrackFavouriteService trackFavouriteService;

    @PutMapping
    public ResponseEntity<String> addTrackToFavourite(Map<String, String> request){
        Long trackId = Long.parseLong(request.get("trackId"));
        return ResponseEntity.ok(trackFavouriteService.addTrackToFavourite(trackId));
    }

    @DeleteMapping
    public ResponseEntity<String> removeTrackFromFavourite(Map<String, String> request){
        Long trackId = Long.parseLong(request.get("trackId"));
        return ResponseEntity.ok(trackFavouriteService.removeTrackFromFavourite(trackId));
    }

}
