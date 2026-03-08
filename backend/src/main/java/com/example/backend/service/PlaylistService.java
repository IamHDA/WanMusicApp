package com.example.backend.service;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;

import java.util.List;

public interface PlaylistService {
    PlaylistDTO getPlaylistById(Long playlistId);
    List<PlaylistPreviewDTO> getPlaylistsByOwnerId(Long ownerId);
    int countPlaylistsByOwnerId(Long ownerId);
    Long createPlaylist(String name);
    String deletePlaylist(Long playlistId);
}
