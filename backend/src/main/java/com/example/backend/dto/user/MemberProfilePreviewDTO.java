package com.example.backend.dto.user;

import com.example.backend.Enum.FriendStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberProfilePreviewDTO {
    private Long id;
    private String avatarUrl;
    private String name;
    private FriendStatus friendStatus;
}
