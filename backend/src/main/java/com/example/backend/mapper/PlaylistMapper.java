package com.example.backend.mapper;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.track.TrackDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.PlaylistCollaborator;
import com.example.backend.entity.PlaylistTrack;
import com.example.backend.service.S3StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MemberMapper.class, TrackMapper.class})
public abstract class PlaylistMapper {

    @Autowired
    private S3StorageService s3StorageService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private TrackMapper trackMapper;

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapKeyToUrl")
    public abstract PlaylistPreviewDTO toPlaylistPreviewDTO(Playlist playlist);
    public abstract List<PlaylistPreviewDTO> toPlaylistPreviewDTOList(List<Playlist> playlistList);

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapKeyToUrl")
    @Mapping(source = "collaborators", target = "collaborators", qualifiedByName = "mapCollaboratorToMemberPreviewDTO")
    @Mapping(source = "tracks", target = "tracks", qualifiedByName = "mapPlaylistTrackToTrackDTO")
    public abstract PlaylistDTO toPlaylistDTO(Playlist playlist);

    @Named("mapKeyToUrl")
    protected String mapKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "thumbnails");
    }

    @Named("mapPlaylistTrackToTrackDTO")
    protected List<TrackDTO> mapPlaylistTrackToTrackDTO(List<PlaylistTrack> playlistTracks){
        return playlistTracks.stream()
                .map(pt -> trackMapper.toTrackDTO(pt.getTrack()))
                .toList();
    }

    @Named("mapCollaboratorToMemberPreviewDTO")
    protected List<MemberProfilePreviewDTO> mapCollaboratorToMemberPreviewDTO(List<PlaylistCollaborator> collaborators){
        return collaborators.stream()
                .map(m -> memberMapper.toPreviewDTO(m.getCollaborator()))
                .toList();
    }
}
