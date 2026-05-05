package com.example.backend.repository;

import com.example.backend.entity.PlaylistTrack;
import com.example.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    @Query("SELECT pt.track FROM PlaylistTrack pt " +
            "WHERE pt.playlist.id = :playlistId " +
            "ORDER BY pt.position")
    List<Track> findAllTrackByPlaylistId(@Param("playlistId") Long playlistId);

    void deleteByPlaylistIdAndTrackId(Long playlistId, Long trackId);
}
