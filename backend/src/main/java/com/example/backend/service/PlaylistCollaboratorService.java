package com.example.backend.service;

import com.example.backend.dto.UpdateCollaboratorRequestDTO;

public interface PlaylistCollaboratorService {

    String addCollaboratorToPlaylist(UpdateCollaboratorRequestDTO dto);
    String removeCollaboratorFromPlaylist(UpdateCollaboratorRequestDTO dto);

}
