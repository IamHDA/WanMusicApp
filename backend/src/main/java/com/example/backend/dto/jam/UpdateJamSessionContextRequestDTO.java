package com.example.backend.dto.jam;

public record UpdateJamSessionContextRequestDTO (
        Long jamId,
        Long trackId,
        Long playlistId,
        Long albumId
){
}
