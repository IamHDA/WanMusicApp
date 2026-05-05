package com.example.backend.mapper;

import com.example.backend.dto.jam.JamNotificationDTO;
import com.example.backend.entity.JamNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JamNotificationMapper {
    @Mapping(source = "id", target = "jamNotificationId")
    JamNotificationDTO toDTO(JamNotification jamNotification);
}
