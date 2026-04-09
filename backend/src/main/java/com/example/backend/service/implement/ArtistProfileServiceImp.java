package com.example.backend.service.implement;

import com.example.backend.Enum.ArtistProfileStatus;
import com.example.backend.dto.album.GetAlbumsPaginationRequest;
import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.CreateArtistProfileRequestDTO;
import com.example.backend.dto.user.UpdateArtistProfileRequestDTO;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.entity.Member;
import com.example.backend.mapper.ArtistProfileMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImp implements ArtistProfileService {

    private final ArtistProfileMapper artistProfileMapper;
    private final AlbumService albumService;
    private final ArtistProfileRepository artistProfileRepo;
    private final MemberRepository memberRepo;
    private final AuthenticationService authenticationService;
    private final S3StorageService s3StorageService;
    private final RedisService redisService;

    @Override
    public ArtistProfileDTO getProfile(Long artistId) {
        String key = "/artist/profile/" + artistId;
        ArtistProfileDTO dto = null;
        if (redisService.hasKey(key))
            return (ArtistProfileDTO) redisService.get(key);

        ArtistProfile profile = artistProfileRepo.findById(artistId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        dto = artistProfileMapper.toArtistProfileDTO(profile);
        dto.setAlbums(albumService.getAlbumsByArtistId(new GetAlbumsPaginationRequest(artistId, 1, 4)));

        redisService.save(key, dto, 60);

        return dto;
    }

    @Override
    public ArtistProfileDTO getMyProfile() {
        Long currentUserId = authenticationService.getCurrentMemberId();
        ArtistProfile profile = artistProfileRepo.findByMemberId(currentUserId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        Long artistId =  profile.getId();
        String key = "/artist/myProfile/" + artistId;
        ArtistProfileDTO dto = null;
        if (redisService.hasKey(key))
            return (ArtistProfileDTO) redisService.get(key);

        dto = artistProfileMapper.toArtistProfileDTO(profile);
        dto.setAlbums(albumService.getAlbumsByArtistId(new GetAlbumsPaginationRequest(artistId, 1, 4)));

        redisService.save(key, dto, 60);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArtistProfileRequest(CreateArtistProfileRequestDTO dto) {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Optional<ArtistProfile> existing = artistProfileRepo.findByMemberId(currentUserId);

        ArtistProfile profile = new ArtistProfile();
        if (existing.isPresent()) {
            profile = existing.get();
            // Đang chờ → KHÔNG cho tạo mới
            if (profile.getStatus() == ArtistProfileStatus.PENDING) {
                throw new RuntimeException("Artist profile pending verification");
            }
            // Bị Reject → Ghi đè lên bản ghi cũ, Reset về PENDING
        } else {
            profile = new ArtistProfile();
            Member member = memberRepo.findById(currentUserId).orElseThrow(()-> new RuntimeException("Member not found!"));
            profile.setMember(member);
        }

        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        profile.setMember(member);
        profile.setStageName(dto.stageName());
        profile.setBio(dto.bio());
        if (existing.isPresent()) {
            if(existing.get().getAvatarKey() != null) s3StorageService.deleteFile(profile.getAvatarKey(), "avatars");
            if(existing.get().getCoverKey() != null) s3StorageService.deleteFile(profile.getCoverKey(), "covers");
        }
        profile.setAvatarKey(dto.avatarKey());
        profile.setCoverKey(dto.coverKey());
        profile.setStatus(ArtistProfileStatus.PENDING);
        profile.setCreatedAt(LocalDateTime.now());

        artistProfileRepo.save(profile);

        return "Send request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateProfile(UpdateArtistProfileRequestDTO dto) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        ArtistProfile profile = artistProfileRepo.findByMemberId(currentUserId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        // Check and delete old avatar
        if (dto.avatarKey() != null && !dto.avatarKey().isBlank()) {
            if (profile.getAvatarKey() != null) {
                s3StorageService.deleteFile(profile.getAvatarKey(), "avatars");
            }
            profile.setAvatarKey(dto.avatarKey());
        }

        // Check and delete old cover
        if (dto.coverKey() != null && !dto.coverKey().isBlank()) {
            if (profile.getCoverKey() != null) {
                s3StorageService.deleteFile(profile.getCoverKey(), "covers");
            }
            profile.setCoverKey(dto.coverKey());
        }

        if (dto.stageName() != null && !dto.stageName().isBlank()) {
            profile.setStageName(dto.stageName());
        }

        if (dto.bio() != null && !dto.bio().isBlank()) {
            profile.setBio(dto.bio());
        }

        artistProfileRepo.save(profile);
        return "Artist Profile updated successfully!";
    }
}
