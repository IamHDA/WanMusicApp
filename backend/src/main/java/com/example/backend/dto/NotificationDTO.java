package com.example.backend.dto;

import com.example.backend.Enum.NotificationType;
import com.example.backend.entity.EmbeddedId.FriendshipId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {

    private Long notificationId;
    private Long jamSessionId;
    private Long trackId;
    private Long playlistId;
    private FriendshipId friendRequestId;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;

}
