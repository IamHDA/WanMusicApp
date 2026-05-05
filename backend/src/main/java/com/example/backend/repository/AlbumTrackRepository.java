package com.example.backend.repository;

import com.example.backend.dto.track.TrackDTO;
import com.example.backend.entity.AlbumTrack;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, AlbumTrackId> {
    Optional<AlbumTrack> findByAlbum_IdAndTrack_Id(Long albumId, Long trackId);

    List<AlbumTrack> findByAlbum_Id(Long albumId);
}
