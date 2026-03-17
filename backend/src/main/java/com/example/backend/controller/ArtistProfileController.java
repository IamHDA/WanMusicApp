package com.example.backend.controller;

import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.service.ArtistProfileService;
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

    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody MemberUpdateProfileDTO dto){
        return ResponseEntity.ok(artistProfileService.updateProfile(dto));
    }

}
