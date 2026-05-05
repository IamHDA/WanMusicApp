package com.example.backend.repository;

import com.example.backend.entity.JamNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JamNotificationRepository extends JpaRepository <JamNotification, Long> {
    @Query("SELECT jn FROM JamNotification jn " +
            "WHERE jn.jamSession.id = :jamSessionId " +
            "ORDER BY jn.createdAt DESC"
    )
    Page<JamNotification> findByJamSessionId(@Param("jamSessionId") Long jamSessionId, Pageable pageable);

    void deleteByJamSession_Id(Long jamSessionId);
}
