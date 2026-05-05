package com.example.backend.service;

import com.example.backend.dto.jam.UpdateJamPlayerRequestDTO;

public interface JamPlayerStateService {
    void updateJamPlayerState(Long jamSessionId, int currentSeekPosition, boolean isPlaying);
    void rejectJamMemberRequest(UpdateJamPlayerRequestDTO dto);
    void acceptJamMemberRequest(UpdateJamPlayerRequestDTO dto);
}
