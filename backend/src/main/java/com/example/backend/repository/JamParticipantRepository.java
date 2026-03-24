package com.example.backend.repository;

import com.example.backend.entity.JamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JamParticipantRepository extends JpaRepository <JamParticipant, Long>{
}
