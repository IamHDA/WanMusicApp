package com.example.backend.controller;

import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.CreateArtistProfileRequestDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.dto.user.UpdateArtistProfileRequestDTO;
import com.example.backend.service.ArtistProfileService;
import com.example.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artist")
@RequiredArgsConstructor
public class ArtistProfileController {

    private final ArtistProfileService artistProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<ArtistProfileDTO> getProfile(@PathVariable Long id){
        return ResponseEntity.ok(artistProfileService.getProfile(id));
    }

    @GetMapping("/myProfile")
    public ResponseEntity<ArtistProfileDTO> getMyProfile(){
        return ResponseEntity.ok(artistProfileService.getMyProfile());
    }

    @PostMapping
    public ResponseEntity<String> sendCreateArtistProfileRequest(@RequestBody CreateArtistProfileRequestDTO dto){
        return ResponseEntity.ok(artistProfileService.createArtistProfileRequest(dto));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateArtistProfileRequestDTO dto){
        return ResponseEntity.ok(artistProfileService.updateProfile(dto));
    }

}
