package com.example.backend.service;

import com.example.backend.dto.jam.CreateJamSessionRequestDTO;

public interface JamSessionService {

    String createJamSession(CreateJamSessionRequestDTO dto);

}
