package com.example.backend.repository;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository <Track, Long>{

    @EntityGraph(attributePaths = {
            "contributions",
            "contributions.contributor"
    })
    @Query("""
        SELECT t FROM Track t
        WHERE t.status = :status
        AND NOT EXISTS (
            SELECT 1 FROM AlbumTrack at
            WHERE at.track = t
        )
    """)
    Page<Track> findAllByStatusAndNotInAlbum(
            @Param("status") TrackStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT t FROM Track t
        WHERE (:ids IS NULL OR t.id NOT IN :ids)
        AND LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND t.status = com.example.backend.Enum.TrackStatus.PUBLISHED
    """)
    Page<Track> search(
            @Param("ids") List<Long> ids,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
