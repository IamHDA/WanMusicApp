package com.example.backend.repository;

import com.example.backend.entity.JamPlayerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JamPlayerStateRepository extends JpaRepository<JamPlayerState, Long> {
    Optional<JamPlayerState> findByJamSessionId(Long jamSessionId);
}
