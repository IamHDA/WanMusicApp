package com.example.backend.service;

import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.CreateArtistProfileRequestDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;

public interface ArtistProfileService {
    ArtistProfileDTO getProfile(Long artistId);
    String createArtistProfileRequest(CreateArtistProfileRequestDTO dto);
    String updateProfile(MemberUpdateProfileDTO dto);
}
