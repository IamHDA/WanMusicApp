package com.example.backend.service;

import com.example.backend.Enum.InteractionType;
import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;

public interface UserInteractionService {

    void addInteraction(Member member, Track track, InteractionType type);

    String addInteraction(CreateInteractionRequestDTO dto);
}
