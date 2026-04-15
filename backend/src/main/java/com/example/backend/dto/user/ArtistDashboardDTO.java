package com.example.backend.dto.user;

import com.example.backend.dto.track.TrackArtistDTO;
import java.util.List;

public record ArtistDashboardDTO(
    long totalFans,
    long totalDrops,
    List<TrackArtistDTO> tracks
) {}
