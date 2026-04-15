package com.example.backend.service;

import com.example.backend.dto.UpdateCollaboratorPermissionRequestDTO;
import com.example.backend.dto.UpdateCollaboratorRequestDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;

import java.util.List;

public interface PlaylistCollaboratorService {

    String addCollaboratorToPlaylist(UpdateCollaboratorRequestDTO dto);
    String removeCollaboratorFromPlaylist(UpdateCollaboratorRequestDTO dto);
    String updateCollaboratorPermissions(UpdateCollaboratorPermissionRequestDTO dto);
    String revokeCollaboratorPermissions(UpdateCollaboratorPermissionRequestDTO dto);

    List<MemberProfilePreviewDTO> getPlaylistColabborators(Long playlistId);
}
