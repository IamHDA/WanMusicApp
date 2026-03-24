package com.example.backend.dto.playlist;

import com.example.backend.dto.track.TrackDTO;
import com.example.backend.dto.user.UserPreviewDTO;

import java.util.List;

public record PlaylistDTO (
        Integer id,
        String title,
        String thumbnailUrl,
        String description,
        UserPreviewDTO owner,
        List<UserPreviewDTO> collaborators,
        List<TrackDTO> tracks
){
}
