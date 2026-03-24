package com.example.backend.repository;

import com.example.backend.entity.JamNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JamNotificationRepository extends JpaRepository <JamNotification, Long> {
}
