package com.example.backend.mapper;

import com.example.backend.dto.user.*;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.service.S3StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ArtistProfileMapper {

    @Autowired
    private S3StorageService s3StorageService;

    @Mapping(source = "stageName", target = "name")
    public abstract TrackContributorDTO toTrackContributorDTO(ArtistProfile artistProfile);

    @Mapping(source = "stageName", target = "name")
    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "mapAvatarKeyToUrl")
    public abstract ArtistProfilePreviewDTO toPreviewDTO(ArtistProfile artistProfile);

    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "mapAvatarKeyToUrl")
    @Mapping(source = "coverKey", target = "coverUrl", qualifiedByName = "mapCoverKeyToUrl")
    @Mapping(target = "albums", ignore = true)
    public abstract ArtistProfileDTO toArtistProfileDTO(ArtistProfile artistProfile);

    @Named("mapAvatarKeyToUrl")
    protected String mapAvatarKeyToUrl(String avatarKey){
        if(avatarKey == null) return null;
        return s3StorageService.getGetPresignedUrl(avatarKey, "avatars");
    }

    @Named("mapCoverKeyToUrl")
    protected String mapCoverKeyToUrl(String coverKey){
        if(coverKey == null) return null;
        return s3StorageService.getGetPresignedUrl(coverKey, "covers");
    }

    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "mapAvatarKeyToUrl")
    public abstract AdminArtistProfilePreviewDTO toAdminPreviewDTO(ArtistProfile artistProfile);

    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "mapAvatarKeyToUrl")
    @Mapping(source = "coverKey", target = "coverUrl", qualifiedByName = "mapCoverKeyToUrl")
    public abstract AdminArtistProfileDTO toAdminDetailDTO(ArtistProfile artistProfile);
}
