package com.example.backend.mapper;

import com.example.backend.dto.TagDTO;
import com.example.backend.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDTO toDTO(Tag tag);
    Tag toEntity(TagDTO tagDTO);
}
