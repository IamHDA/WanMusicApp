package com.example.backend.service;

import com.example.backend.dto.CreateJamInvitationRequestDTO;
import com.example.backend.dto.jam.JamParticipantRequestDTO;

public interface JamParticipantService {

    String joinJamById(JamParticipantRequestDTO request);
    String inviteMember(CreateJamInvitationRequestDTO request);
    String leaveJam(JamParticipantRequestDTO request);

    String joinJamByCode(JamParticipantRequestDTO dto);
}
