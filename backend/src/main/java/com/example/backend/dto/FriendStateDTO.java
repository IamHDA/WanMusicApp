package com.example.backend.dto;

import com.example.backend.Enum.UserStatus;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendStateDTO {

    private MemberProfilePreviewDTO member;
    private TrackPreviewDTO currentTrack;
    private UserStatus status;

}
