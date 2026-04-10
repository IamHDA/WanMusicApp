package com.example.backend.service;

import com.example.backend.Enum.InteractionType;
import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;

public interface UserInteractionService {

    String addInteraction(CreateInteractionRequestDTO dto);
}
