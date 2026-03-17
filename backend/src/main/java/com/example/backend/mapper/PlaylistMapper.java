package com.example.backend.mapper;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.user.MemberPreviewDTO;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.PlaylistCollaborator;
import com.example.backend.service.S3StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MemberMapper.class})
public abstract class PlaylistMapper {

    @Autowired
    private S3StorageService s3StorageService;
    @Autowired
    private MemberMapper memberMapper;

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapKeyToUrl")
    public abstract PlaylistPreviewDTO toPlaylistPreviewDTO(Playlist playlist);
    public abstract List<PlaylistPreviewDTO> toPlaylistPreviewDTOList(List<Playlist> playlistList);

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapKeyToUrl")
    @Mapping(source = "collaborators", target = "collaborators", qualifiedByName = "mapCollaboratorToUserPreviewDTO")
    public abstract PlaylistDTO toPlaylistDTO(Playlist playlist);

    @Named("mapKeyToUrl")
    protected String mapKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "thumbnail");
    }

    @Named("mapCollaboratorToUserPreviewDTO")
    protected List<MemberPreviewDTO> mapCollaboratorToUserPreviewDTO(List<PlaylistCollaborator> collaborators){
        return collaborators.stream()
                .map(m -> memberMapper.toPreviewDTO(m.getCollaborator()))
                .toList();
    }
}
