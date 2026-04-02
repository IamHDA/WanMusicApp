package com.example.backend.repository;

import com.example.backend.entity.PlayerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerStateRepository extends JpaRepository<PlayerState, Long> {
    Optional<PlayerState> findByMemberId(Long currentMemberId);
}
