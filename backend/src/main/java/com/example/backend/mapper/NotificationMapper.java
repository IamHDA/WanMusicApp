package com.example.backend.mapper;

import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "id", target = "notificationId")
    @Mapping(source = "jamSession.id", target = "jamSessionId")
    @Mapping(source = "track.id", target = "trackId")
    @Mapping(source = "playlist.id", target = "playlistId")
    @Mapping(source = "friendship.id", target = "friendRequestId")
    NotificationDTO toDTO(Notification notification);

}
