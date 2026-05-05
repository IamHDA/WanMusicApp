package com.example.backend.service;

import com.example.backend.dto.user.AdminArtistProfileDTO;
import com.example.backend.dto.user.AdminArtistProfilePreviewDTO;

import java.util.List;

public interface AdminService {

    String approveTrackRequest(Long trackId);
    String rejectTrackRequest(Long trackId);
    String approveArtistProfileRequest(Long artistProfileId);
    String rejectArtistProfileRequest(Long artistProfileId);
    String approveAlbumRequest(Long albumId);
    String rejectAlbumRequest(Long albumId);

    List<AdminArtistProfilePreviewDTO> getAllPendingArtistProfile();
    AdminArtistProfileDTO getArtistProfileDetail(Long id);

}
