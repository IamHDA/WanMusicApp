package com.example.backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistProfilePreviewDTO {
    private Long id;
    private String avatarUrl;
    private String name;
    private boolean isFollowed;
}
