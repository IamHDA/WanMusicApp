package com.example.backend.service;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.user.MemberProfilePreviewDTO;

import java.util.List;

public interface FollowerService {
    int countFollowedArtistByUserId(Long userId);
    String followArtist(Long artistId);
    String unfollowArtist(Long artistId);
    PageResponse<MemberProfilePreviewDTO> getArtistFollower(Long artistId, int page, int size);
}
