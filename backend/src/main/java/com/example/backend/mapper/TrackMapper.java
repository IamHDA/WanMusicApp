package com.example.backend.mapper;

import com.example.backend.dto.ContributorDTO;
import com.example.backend.dto.TagDTO;
import com.example.backend.dto.track.TrackAdminReviewDTO;
import com.example.backend.dto.track.TrackDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.ArtistProfilePreviewDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.entity.ArtistContribution;
import com.example.backend.entity.Track;
import com.example.backend.entity.TrackTag;
import com.example.backend.service.S3StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TrackMapper {

    @Autowired
    private ArtistProfileMapper artistProfileMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private S3StorageService s3StorageService;

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "contributions", target = "contributors", qualifiedByName = "mapTrackContributionsToNamePreview")
    public abstract TrackPreviewDTO toTrackPreviewDTO(Track track);

    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "contributions", target = "contributors", qualifiedByName = "mapTrackContributionsToNamePreview")
    public abstract TrackDTO toTrackDTO(Track track);

    @Mapping(source = "id", target = "trackId")
    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "tags", target = "recommendedTags", qualifiedByName = "mapTrackTagToTagDTO")
    @Mapping(source = "contributions", target = "featuredArtists", qualifiedByName = "mapContributionToProfilePreview")
    public abstract TrackDraftResponseDTO toTrackDraftResponse(Track track);

    @Mapping(source = "id", target = "trackId")
    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTrackTagToTagDTO")
    public abstract TrackAdminReviewDTO toTrackAdminReviewDTO(Track track);

    @Named("mapTrackKeyToUrl")
    protected String mapTrackKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "songs");
    }

    @Named("mapThumbnailKeyToUrl")
    protected String mapThumbnailKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "thumbnails");
    }

    @Named("mapTrackTagToTagDTO")
    protected List<TagDTO> mapTrackTagToTagDTO(List<TrackTag> trackTags){
        return trackTags.stream()
                .map(tt -> tagMapper.toDTO(tt.getTag()))
                .toList();
    }

    @Named("mapTrackContributionsToNamePreview")
    protected List<ContributorDTO> mapTrackContributionsToNamePreview(List<ArtistContribution> contributions){
        return contributions.stream()
                .map(ac -> {
                    ContributorDTO dto = artistProfileMapper.toTrackContributorDTO(ac.getContributor());
                    dto.setRole(ac.getRole().toString());
                    return dto;
                })
                .toList();
    }

    @Named("mapContributionToProfilePreview")
    protected List<ArtistProfilePreviewDTO> mapContributionToProfilePreview(List<ArtistContribution> contributions){
        return contributions.stream()
                .map(artistContribution -> artistProfileMapper.toPreviewDTO(artistContribution.getContributor()))
                .toList();
    }
}
