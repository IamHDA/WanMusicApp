package com.example.backend.dto.jam;

public record GetJamNotificationRequestDTO (
        Long jamId,
        int index,
        int size
) {
}
