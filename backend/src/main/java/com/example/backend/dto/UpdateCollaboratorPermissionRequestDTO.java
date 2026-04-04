package com.example.backend.dto;

import java.util.List;

public record UpdateCollaboratorPermissionRequestDTO(
        Long playlistId,
        Long collaboratorId,
        List<String> permissions
) {}
