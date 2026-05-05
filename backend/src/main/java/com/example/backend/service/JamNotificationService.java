package com.example.backend.service;

import com.example.backend.dto.CreateJamNotificationDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.jam.GetJamNotificationRequestDTO;
import com.example.backend.dto.jam.JamNotificationDTO;

import java.security.Principal;

public interface JamNotificationService {
    JamNotificationDTO sendJamNotification(CreateJamNotificationDTO dto, String email);
    PageResponse<JamNotificationDTO> getJamNotifications(GetJamNotificationRequestDTO dto);
}
