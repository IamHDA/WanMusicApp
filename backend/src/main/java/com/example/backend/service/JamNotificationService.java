package com.example.backend.service;

import com.example.backend.dto.CreateJamNotificationDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.jam.GetJamNotificationRequestDTO;
import com.example.backend.dto.jam.JamNotificationDTO;

public interface JamNotificationService {
    void sendJamNotification(CreateJamNotificationDTO dto);
    PageResponse<JamNotificationDTO> getJamNotifications(GetJamNotificationRequestDTO dto);
}
