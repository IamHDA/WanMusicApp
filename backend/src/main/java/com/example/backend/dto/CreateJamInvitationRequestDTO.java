package com.example.backend.dto;

import java.util.List;

public record CreateJamInvitationRequestDTO(
        Long jamSessionId,
        List<Long> memberIds
) {
}
