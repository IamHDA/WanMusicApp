package com.example.backend.repository;

import com.example.backend.entity.SharedPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedPlaylistRepository extends JpaRepository<SharedPlaylist, Long> {
}
