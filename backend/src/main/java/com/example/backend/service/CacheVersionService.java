package com.example.backend.service;

public interface CacheVersionService {
    long getJamNotificationVersion(Long jamSessionId);
    void bumpJamNotificationVersion(Long jamSessionId);
    long getTrackVersion();
    void bumpTrackVersion();
    long getArtistVersion();
    void bumpArtistVersion();
    long getAlbumVersion();
    void bumpAlbumVersion();
    long getMemberVersion();
    void bumpMemberVersion();
    long getFriendVersion();
    void bumpFriendVersion();
}
