package com.example.backend.dto.playlistTrack;

import java.util.List;

public record PlaylistTrackDTO (
        List<Long> playlistId,
        Long trackId
){
}
