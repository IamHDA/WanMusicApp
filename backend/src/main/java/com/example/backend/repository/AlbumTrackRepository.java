package com.example.backend.repository;

import com.example.backend.entity.AlbumTrack;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, AlbumTrackId> {
}
