package com.example.backend.dto;

import com.example.backend.Enum.InterationType;
import lombok.Getter;

@Getter
public class AddInteractionRequestDTO {

    private InterationType type;
    private int duration;
    private int trackId;

}
