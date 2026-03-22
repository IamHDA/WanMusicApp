package com.example.backend.repository;

import com.example.backend.entity.PlaylistTrack;
import com.example.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    @Query("SELECT pt.track FROM PlaylistTrack pt WHERE pt.playlist.id = :playlistId")
    List<Track> findAllTrackByPlaylistId(@Param("playlistId") Long playlistId);

    PlaylistTrack findByPlaylistId(Long playlistId);
    PlaylistTrack findByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);
}
