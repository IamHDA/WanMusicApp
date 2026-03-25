package com.example.backend.repository;

import com.example.backend.entity.EmbeddedId.TrackFavouriteId;
import com.example.backend.entity.TrackFavourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackFavouriteRepository extends JpaRepository<TrackFavourite, TrackFavouriteId> {
    TrackFavourite findByMember_IdAndTrack_Id(Long currentMemberId, Long trackId);
}
