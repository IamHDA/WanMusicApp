package com.example.backend.service;

import com.example.backend.dto.CreateJamNotificationRequestDTO;
import com.example.backend.dto.JamNotificationDTO;

public interface JamNotificationService {
    JamNotificationDTO sendJamNotification(CreateJamNotificationRequestDTO dto);
}
