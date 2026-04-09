package com.example.backend.service;

public interface SearchCacheVersionService {
    long getTrackVersion();
    void bumpTrackVersion();
    long getArtistVersion();
    void bumpArtistVersion();
    long getAlbumVersion();
    void bumpAlbumVersion();
}
