package com.example.backend.dto.jam;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.JamInteractionStatus;
import com.example.backend.Enum.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JamNotificationDTO {
    private Long jamSessionId;
    private Long jamNotificationId;
    private Long trackId;
    private Long senderId;
    private String message;
    private NotificationType type;
    private InteractionType interactionType;
    private JamInteractionStatus status;
    private Integer duration;
    private LocalDateTime createdAt;
}
