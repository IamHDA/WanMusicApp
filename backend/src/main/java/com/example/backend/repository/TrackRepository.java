package com.example.backend.repository;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository <Track, Long>{
    Page<Track> findAllByStatus(TrackStatus status, Pageable pageable);
    Page<Track> findAllByIdNotInAndTitleContainingIgnoreCase(List<Long> trackIds, String title, Pageable pageable);
}
