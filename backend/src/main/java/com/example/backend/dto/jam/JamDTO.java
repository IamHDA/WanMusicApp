package com.example.backend.dto.jam;

import com.example.backend.dto.PlayerStateDTO;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JamDTO {

    private Long id;
    private String code;
    private boolean isPublic;
    private int size;
    private List<MemberProfilePreviewDTO> members;
    private MemberProfilePreviewDTO owner;
    private JamTrackPreviewDTO jamTrack;
    private PlaylistPreviewDTO currentPlaylist;
    private AlbumPreviewDTO currentAlbum;
}
