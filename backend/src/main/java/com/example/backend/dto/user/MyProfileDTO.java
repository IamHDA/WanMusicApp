package com.example.backend.dto.user;

import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import lombok.Data;

import java.util.List;

@Data
public class MyProfileDTO {
    private Long id;
    private String displayName;
    private String avatarUrl;
    private String subscriptionType;
    
    private String friendStatus; // Mặc định là SELF
    private int followedArtistCount;
    private int friendCount;
    private int playlistCount;
    private List<PlaylistPreviewDTO> playlists;

    // Các trường đặc quyền chỉ có chủ tài khoản mới thấy được (Dùng cho Artist Portal)
    private String artistStatus;
    private String artistStageName;
    private String artistBio;
    private String artistAvatarUrl;
    private String artistCoverUrl;
}
