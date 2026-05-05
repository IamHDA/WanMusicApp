package com.example.backend.controller;

import com.example.backend.dto.UpdateCollaboratorPermissionRequestDTO;
import com.example.backend.dto.UpdateCollaboratorRequestDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.service.PlaylistCollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playlist-collaborator")
public class PlaylistCollaboratorController {

    private final PlaylistCollaboratorService playlistCollaboratorService;

    @GetMapping("/getCollaborators/{playlistId}")
    public ResponseEntity<List<MemberProfilePreviewDTO>> getPlaylistCollaborators(@PathVariable Long playlistId){
        return ResponseEntity.ok(playlistCollaboratorService.getPlaylistColabborators(playlistId));
    }

    @GetMapping("/getCollaboratorPermissions")
    public ResponseEntity<List<String>> getCollaboratorPermission(
            @RequestParam(name = "collabId") Long collabId,
            @RequestParam(name = "playlistId") Long playlistId
    ){
        return ResponseEntity.ok(playlistCollaboratorService.getCollaboratorPermissions(collabId, playlistId));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCollaboratorToPlaylist(@RequestBody UpdateCollaboratorRequestDTO dto){
        return ResponseEntity.ok(playlistCollaboratorService.addCollaboratorToPlaylist(dto));
    }

    @PutMapping("/updatePermission")
    public ResponseEntity<String> updateCollaboratorPermission(@RequestBody UpdateCollaboratorPermissionRequestDTO dto){
        return ResponseEntity.ok(playlistCollaboratorService.updateCollaboratorPermissions(dto));
    }

    @PutMapping("/revokePermission")
    public ResponseEntity<String> revokeCollaboratorPermission(@RequestBody UpdateCollaboratorPermissionRequestDTO dto){
        return ResponseEntity.ok(playlistCollaboratorService.revokeCollaboratorPermissions(dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> removeCollaboratorFromPlaylist(@RequestBody UpdateCollaboratorRequestDTO dto){
        return ResponseEntity.ok(playlistCollaboratorService.removeCollaboratorFromPlaylist(dto));
    }

}
