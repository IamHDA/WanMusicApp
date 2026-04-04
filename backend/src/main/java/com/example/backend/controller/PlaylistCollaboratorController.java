package com.example.backend.controller;

import com.example.backend.dto.UpdateCollaboratorPermissionRequestDTO;
import com.example.backend.dto.UpdateCollaboratorRequestDTO;
import com.example.backend.service.PlaylistCollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playlist-collaborator")
public class PlaylistCollaboratorController {

    private final PlaylistCollaboratorService playlistCollaboratorService;

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
