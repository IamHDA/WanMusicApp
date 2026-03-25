package com.example.backend.dto;

import com.example.backend.Enum.InteractionType;
import lombok.Getter;

@Getter
public class AddInteractionRequestDTO {

    private Long trackId;
    private InteractionType type;
    private int duration;

}
