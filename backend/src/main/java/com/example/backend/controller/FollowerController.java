package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follower")
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerService followerService;

    @GetMapping("/artistId")
    public ResponseEntity<PageResponse<MemberProfilePreviewDTO>> getArtistFollowers(
            @RequestParam("artistId") Long artistId,
            @RequestParam("index") Integer index,
            @RequestParam("size") Integer size
    ){
        return ResponseEntity.ok(followerService.getArtistFollower(artistId, index, size));
    }

    @PostMapping("/{artistId}")
    public ResponseEntity<String> followArtist(@PathVariable Long artistId){
        return ResponseEntity.ok(followerService.followArtist(artistId));
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<String> unfollowArtist(@PathVariable Long artistId){
        return ResponseEntity.ok(followerService.unfollowArtist(artistId));
    }

}
