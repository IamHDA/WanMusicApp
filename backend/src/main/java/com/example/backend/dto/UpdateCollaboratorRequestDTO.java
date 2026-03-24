package com.example.backend.dto;

import java.util.Set;

public record UpdateCollaboratorRequestDTO(
        Long playlistId,
        Set<Long> userIds
) {
}
