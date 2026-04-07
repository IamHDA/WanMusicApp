package com.example.backend.repository;

import com.example.backend.Enum.ArtistProfileStatus;
import com.example.backend.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, Long> {
    Optional<ArtistProfile> findByMemberId(Long memberId);

    List<ArtistProfile> findByStatus(ArtistProfileStatus status);
}
