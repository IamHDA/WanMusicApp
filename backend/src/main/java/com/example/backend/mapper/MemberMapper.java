package com.example.backend.mapper;

import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.MemberProfileDTO;
import com.example.backend.entity.Member;
import com.example.backend.service.S3StorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class MemberMapper {

    @Autowired
    private S3StorageService s3StorageService;

    @Mapping(source = "fullName", target = "displayName")
    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "memberAvatarKeyToUrl")
    public abstract MemberProfileDTO toProfileDTO(Member member);

    @Mapping(source = "fullName", target = "name")
    @Mapping(source = "avatarKey", target = "avatarUrl", qualifiedByName = "memberAvatarKeyToUrl")
    public abstract MemberProfilePreviewDTO toPreviewDTO(Member member);

    @Named("memberAvatarKeyToUrl")
    protected String mapKeyToUrl(String avatarKey){
        if (avatarKey == null || avatarKey.isBlank()) return null;
        return s3StorageService.getGetPresignedUrl(avatarKey, "avatars");
    }
}
