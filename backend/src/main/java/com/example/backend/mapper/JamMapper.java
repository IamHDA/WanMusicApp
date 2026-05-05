package com.example.backend.mapper;


import com.example.backend.dto.jam.JamDTO;
import com.example.backend.entity.JamSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MemberMapper.class, AlbumMapper.class, PlaylistMapper.class})
public abstract class JamMapper {

    @Mapping(source = "contextAlbum", target = "currentAlbum")
    @Mapping(source = "contextPlaylist", target = "currentPlaylist")
    @Mapping(source = "sessionCode", target = "code")
    public abstract JamDTO toJamDTO(JamSession jamSession);
}
