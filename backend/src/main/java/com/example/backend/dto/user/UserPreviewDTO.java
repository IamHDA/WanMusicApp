package com.example.backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreviewDTO{
    private Long id;
    private String avatarUrl;
    private String name;
    private boolean isFollowed;
    private boolean isFriend;
}
