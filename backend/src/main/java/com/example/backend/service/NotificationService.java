package com.example.backend.service;

import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    void sendNotification(CreateNotificationDTO dto);
    List<NotificationDTO> getNotifications(Long memberId);
}
