package com.example.backend.service;

import com.example.backend.dto.SharePlaylistRequestDTO;

public interface SharedPlaylistService {
    String sharePlaylist(SharePlaylistRequestDTO dto);
}
