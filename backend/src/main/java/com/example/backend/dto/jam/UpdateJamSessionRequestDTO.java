package com.example.backend.dto.jam;

public record UpdateJamSessionRequestDTO(
        Long jamSessionId,
        Integer size,
        Boolean isPublic
){
}
