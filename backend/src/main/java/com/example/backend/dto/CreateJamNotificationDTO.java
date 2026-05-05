package com.example.backend.dto;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.NotificationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateJamNotificationDTO {
    private Long jamId;
    private Long trackId;
    private NotificationType notificationType;
    private InteractionType interactionType;
    private Integer duration;
}
