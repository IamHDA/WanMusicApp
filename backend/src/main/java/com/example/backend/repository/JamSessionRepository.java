package com.example.backend.repository;

import com.example.backend.entity.JamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JamSessionRepository extends JpaRepository <JamSession, Long>{
    JamSession findBySessionCode(String s);
}
