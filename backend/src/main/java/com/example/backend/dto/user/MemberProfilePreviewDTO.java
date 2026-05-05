package com.example.backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberProfilePreviewDTO {
    private Long id;
    private String avatarUrl;
    private String name;
    private String friendStatus;
}
