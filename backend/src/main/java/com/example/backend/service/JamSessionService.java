package com.example.backend.service;

import com.example.backend.dto.jam.CreateJamSessionRequestDTO;
import com.example.backend.dto.jam.JamDTO;
import com.example.backend.dto.jam.UpdateJamSessionRequestDTO;

public interface JamSessionService {

    JamDTO createJamSession(CreateJamSessionRequestDTO dto);
    String updateJamSession(UpdateJamSessionRequestDTO dto);
    String deleteJamSession(Long jamSessionId);
}
