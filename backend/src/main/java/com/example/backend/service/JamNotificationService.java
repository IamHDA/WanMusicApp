package com.example.backend.service;

import com.example.backend.dto.CreateJamNotificationDTO;

public interface JamNotificationService {
    void sendJamNotification(CreateJamNotificationDTO dto);
}
