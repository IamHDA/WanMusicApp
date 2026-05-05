package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.TrackTagId;
import com.example.backend.entity.TrackTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackTagRepository extends JpaRepository<TrackTag, TrackTagId> {
    List<TrackTag> findByTag_IdIn(List<Long> tagIds, Pageable pageable);
    List<TrackTag> findByTrack_Id(Long trackId);
}
