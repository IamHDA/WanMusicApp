package com.example.backend.service;

import com.example.backend.dto.jam.CreateJamSessionRequestDTO;
import com.example.backend.dto.jam.UpdateJamSessionRequestDTO;

public interface JamSessionService {

    String createJamSession(CreateJamSessionRequestDTO dto);
    String updateJamSession(UpdateJamSessionRequestDTO dto);
    String deleteJamSession(Long jamSessionId);
}
