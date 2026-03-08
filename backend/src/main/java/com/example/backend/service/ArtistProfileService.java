package com.example.backend.service;

import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;

public interface ArtistProfileService {
    ArtistProfileDTO getProfile(Long artistId);
    String updateProfile(MemberUpdateProfileDTO dto);
}
