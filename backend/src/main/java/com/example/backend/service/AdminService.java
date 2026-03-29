package com.example.backend.service;

public interface AdminService {

    String approveTrackRequest(Long trackId);
    String rejectTrackRequest(Long trackId);
    String approveArtistProfileRequest(Long artistProfileId);
    String rejectArtistProfileRequest(Long artistProfileId);
    String approveAlbumRequest(Long albumId);
    String rejectAlbumRequest(Long albumId);

}
