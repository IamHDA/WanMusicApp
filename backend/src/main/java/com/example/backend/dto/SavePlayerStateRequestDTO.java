package com.example.backend.dto;

public record SavePlayerStateRequestDTO (
        Integer currentSeekPosition,
        Long trackId,
        Long playlistId,
        Long albumId,
        Long memberId
){
}
