package com.example.backend.dto.playlist;

import com.example.backend.dto.track.TrackDTO;
import com.example.backend.dto.user.MemberPreviewDTO;

import java.util.List;

public record PlaylistDTO (
        Integer id,
        String title,
        String thumbnailUrl,
        String description,
        MemberPreviewDTO owner,
        List<MemberPreviewDTO> collaborators,
        List<TrackDTO> tracks
){
}
