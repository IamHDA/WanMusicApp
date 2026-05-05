package com.example.backend.mapper;

import com.example.backend.dto.TagDTO;
import com.example.backend.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {
    @Mapping(source = "parentTags.id", target = "parentTagId")
    TagDTO toDTO(Tag tag);
    
    @Mapping(target = "parentTags", ignore = true)
    Tag toEntity(TagDTO tagDTO);
}
