package com.example.backend.service;

import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.CreateArtistProfileRequestDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.dto.user.UpdateArtistProfileRequestDTO;

public interface ArtistProfileService {
    ArtistProfileDTO getProfile(Long artistId);

    String createArtistProfileRequest(CreateArtistProfileRequestDTO dto);

    String updateProfile(UpdateArtistProfileRequestDTO dto);
}
