package com.example.backend.repository;

import com.example.backend.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<List<Playlist>> findAllByOwnerId(Long ownerId);
    int countByOwnerId(Long ownerId);
}
