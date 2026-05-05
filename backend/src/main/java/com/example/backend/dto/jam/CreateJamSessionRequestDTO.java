package com.example.backend.dto.jam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJamSessionRequestDTO {

    private int size;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

}
