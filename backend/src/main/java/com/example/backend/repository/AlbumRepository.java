package com.example.backend.repository;

import com.example.backend.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    @EntityGraph(attributePaths = {"artist"})
    Page<Album> findByArtistId(Long artistId, Pageable pageable);
}
