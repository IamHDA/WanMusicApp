package com.example.backend.dto;

import com.example.backend.Enum.NotificationType;
import com.example.backend.entity.EmbeddedId.FriendshipId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationDTO {

    private Long notificationId;
    private Long jamSessionId;
    private Long trackId;
    private FriendshipId friendRequestId;
    private Long playlistId;
    private Long targetId;
    private NotificationType notificationType;
    private String senderName;

}



































