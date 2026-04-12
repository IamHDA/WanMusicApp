package com.example.backend.service.implement;

import com.example.backend.service.CacheVersionService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheVersionServiceImp implements CacheVersionService {
    private final Map<Long, AtomicLong> jamNotificationVersion = new HashMap<>();
    private final AtomicLong trackVersion = new AtomicLong(1);
    private final AtomicLong artistVersion = new AtomicLong(1);
    private final AtomicLong albumVersion = new AtomicLong(1);
    private final AtomicLong friendShipVersion = new AtomicLong(1);
    private final AtomicLong memberVersion = new AtomicLong(1);

    @Override
    public long getJamNotificationVersion(Long jamSessionId) {
        return jamNotificationVersion
                .computeIfAbsent(jamSessionId, id -> new AtomicLong(1))
                .get();
    }

    @Override
    public void bumpJamNotificationVersion(Long jamSessionId) {
        jamNotificationVersion
            .computeIfAbsent(jamSessionId, id -> new AtomicLong(1))
            .incrementAndGet();
    }

    @Override
    public long getTrackVersion() {
        return trackVersion.get();
    }

    @Override
    public void bumpTrackVersion() {
        trackVersion.incrementAndGet();
    }

    @Override
    public long getArtistVersion() {
        return artistVersion.get();
    }

    @Override
    public void bumpArtistVersion() {
        artistVersion.incrementAndGet();
    }

    @Override
    public long getAlbumVersion() {
        return albumVersion.get();
    }

    @Override
    public void bumpAlbumVersion() {
        albumVersion.incrementAndGet();
    }

    @Override
    public long getMemberVersion() {
        return memberVersion.get();
    }

    @Override
    public void bumpMemberVersion() {
        memberVersion.incrementAndGet();
    }

    @Override
    public long getFriendVersion() {
        return friendShipVersion.get();
    }

    @Override
    public void bumpFriendVersion() {
        friendShipVersion.incrementAndGet();
    }
}
