package com.example.backend.dto.jam;

import lombok.Getter;

import java.util.Set;

@Getter
public class InviteParticipantToJamRequestDTO {

    private Long jamSessionId;
    private Set<Long> memberIds;

}
