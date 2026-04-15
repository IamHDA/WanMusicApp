package com.example.backend.repository;

import com.example.backend.Enum.AlbumStatus;
import com.example.backend.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    @EntityGraph(attributePaths = {"artist"})
    @Query("select distinct a from Album a where a.artist.id = :artistId and a.status = :status")
    Page<Album> findByArtistIdAndStatus(Long artistId, AlbumStatus status, Pageable pageable);
}
