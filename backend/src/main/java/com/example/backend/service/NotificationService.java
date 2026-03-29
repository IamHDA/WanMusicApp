package com.example.backend.service;

import com.example.backend.dto.CreateNotificationDTO;

public interface NotificationService {
    void sendNotification(CreateNotificationDTO dto);
}
