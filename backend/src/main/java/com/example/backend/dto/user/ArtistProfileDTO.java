package com.example.backend.dto.user;

import com.example.backend.dto.AlbumDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackDTO;
import lombok.Data;

import java.util.List;

@Data
public class ArtistProfileDTO {
    private Long id;
    private String stageName;
    private String avatarUrl;
    private String coverUrl;
    private int followerCount;
    private List<TrackDTO> popularTracks;
    private PageResponse<AlbumDTO> albums;
}
