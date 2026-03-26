package com.example.backend.dto.user;

import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import lombok.Data;

import java.util.List;

@Data
public class MemberProfileDTO {
    private Long id;
    private String displayName;
    private String avatarUrl;
    private boolean isArtist;
    private String friendStatus;
    private int followedArtistCount;
    private int friendCount;
    private int playlistCount;
    private List<PlaylistPreviewDTO> playlists;
}
