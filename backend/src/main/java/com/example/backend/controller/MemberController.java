package com.example.backend.controller;

import com.example.backend.dto.user.AccountSettingsDTO;
import com.example.backend.dto.user.MemberProfileDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.user.MyProfileDTO;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/getProfile/{id}")
    public ResponseEntity<MemberProfileDTO> getProfile(@PathVariable Long id){
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    @GetMapping("/myProfile")
    public ResponseEntity<MyProfileDTO> getMyProfile(){
        return ResponseEntity.ok(memberService.getMyProfile());
    }

    @PostMapping("/updateProfile")
    public ResponseEntity<String> updateProfile(@RequestBody MemberUpdateProfileDTO dto){
        return ResponseEntity.ok(memberService.updateProfile(dto));
    }

    @PostMapping("/followArtist/{artistId}")
    public ResponseEntity<String> followArtist(@PathVariable Long artistId){
        return ResponseEntity.ok(memberService.followArtist(artistId));
    }

    @GetMapping("/account-setting")
    public ResponseEntity<AccountSettingsDTO> getAccountSettings() {
        return ResponseEntity.ok(memberService.getAccountSettings());
    }
}
