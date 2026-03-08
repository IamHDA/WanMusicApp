package com.example.backend.mapper;

import com.example.backend.dto.TagDTO;
import com.example.backend.dto.track.TrackAdminReviewDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.MemberPreviewDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.dto.user.TrackContributorDTO;
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
    @Mapping(source = "contributions", target = "contributors", qualifiedByName = "mapTrackContributionsToNamePreview")
    public abstract TrackPreviewDTO toTrackDTO(Track track);

    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "tags", target = "recommendedTags", qualifiedByName = "mapTrackTagToTagDTO")
    @Mapping(source = "contributions", target = "featuredArtists", qualifiedByName = "mapContributionToProfilePreview")
    public abstract TrackDraftResponseDTO toTrackDraftResponse(Track track);

    @Mapping(source = "fileKey", target = "trackUrl", qualifiedByName = "mapTrackKeyToUrl")
    @Mapping(source = "thumbnailKey", target = "thumbnailUrl", qualifiedByName = "mapThumbnailKeyToUrl")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "mapTrackTagToTagDTO")
    public abstract TrackAdminReviewDTO toTrackAdminReviewDTO(Track track);

    @Named("mapTrackKeyToUrl")
    protected String mapTrackKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "track");
    }

    @Named("mapThumbnailKeyToUrl")
    protected String mapThumbnailKeyToUrl(String key){
        return s3StorageService.getGetPresignedUrl(key, "thumbnail");
    }

    @Named("mapTrackTagToTagDTO")
    protected List<TagDTO> mapTrackTagToTagDTO(List<TrackTag> trackTags){
        return trackTags.stream()
                .map(tt -> tagMapper.toDTO(tt.getTag()))
                .toList();
    }

    @Named("mapTrackContributionsToNamePreview")
    protected List<TrackContributorDTO> mapTrackContributionsToNamePreview(List<ArtistContribution> contributions){
        return contributions.stream()
                .map(ac -> artistProfileMapper.toTrackContributorDTO(ac.getContributor()))
                .toList();
    }

    @Named("mapContributionToProfilePreview")
    protected List<MemberPreviewDTO> mapContributionToProfilePreview(List<ArtistContribution> contributions){
        return contributions.stream()
                .map(artistContribution -> artistProfileMapper.toPreviewDTO(artistContribution.getContributor()))
                .toList();
    }
}
