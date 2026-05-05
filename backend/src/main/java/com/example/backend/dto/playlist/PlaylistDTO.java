package com.example.backend.dto.playlist;

import com.example.backend.dto.track.TrackDTO;
import com.example.backend.dto.user.ArtistProfilePreviewDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;

import java.util.List;

public record PlaylistDTO (
        Integer id,
        String title,
        String thumbnailUrl,
        String description,
        MemberProfilePreviewDTO owner,
        List<MemberProfilePreviewDTO> collaborators,
        List<TrackDTO> tracks
){
}
