package com.example.backend.service;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.playlist.UpdatePlaylistDetailDTO;

import java.util.List;

public interface PlaylistService {
    PlaylistDTO getPlaylistById(Long playlistId);
    List<PlaylistPreviewDTO> getPlaylistsByOwnerId(Long ownerId);
    int countPlaylistsByOwnerId(Long ownerId);
    Long createPlaylist(String name);
    String updatePlaylistDetail(UpdatePlaylistDetailDTO dto);
    String setPlaylistToPublic(Long playlistId);
    String deletePlaylist(Long playlistId);
}
