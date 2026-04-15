package com.example.backend.service;

import com.example.backend.dto.jam.*;

public interface JamSessionService {

    void updateJamSessionContext(UpdateJamSessionContextRequestDTO dto);
    JamDTO getJamSessionById(Long jamSessionId);
    JamPreviewDTO createJamSession(CreateJamSessionRequestDTO dto);
    String updateJamSession(UpdateJamSessionRequestDTO dto);
    String deleteJamSession(Long jamSessionId);

}
