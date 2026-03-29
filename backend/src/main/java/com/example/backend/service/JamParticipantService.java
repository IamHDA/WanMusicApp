package com.example.backend.service;

import com.example.backend.dto.CreateJamInvitationRequestDTO;
import com.example.backend.dto.jam.AcceptInvitationRequestDTO;

public interface JamParticipantService {

    String joinJam(AcceptInvitationRequestDTO request);
    String inviteMember(CreateJamInvitationRequestDTO request);
}
