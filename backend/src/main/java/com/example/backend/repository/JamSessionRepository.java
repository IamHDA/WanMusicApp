package com.example.backend.repository;

import com.example.backend.entity.JamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JamSessionRepository extends JpaRepository <JamSession, Long>{
    @Query("""
    SELECT js FROM JamSession js
    WHERE js.sessionCode = :code
""")
    JamSession findBySessionCode(@Param("code") String code);
}
