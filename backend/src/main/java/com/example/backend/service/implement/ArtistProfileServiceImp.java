package com.example.backend.service.implement;

import com.example.backend.Enum.ArtistProfileStatus;
import com.example.backend.dto.user.ArtistProfileDTO;
import com.example.backend.dto.user.CreateArtistProfileRequestDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.mapper.ArtistProfileMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.service.AlbumService;
import com.example.backend.service.ArtistProfileService;
import com.example.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImp implements ArtistProfileService {

    private final ArtistProfileMapper artistProfileMapper;
    private final AlbumService albumService;
    private final ArtistProfileRepository artistProfileRepo;
    private final AuthenticationService authenticationService;

    @Override
    public ArtistProfileDTO getProfile(Long artistId) {
        ArtistProfile profile = artistProfileRepo.findById(artistId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        ArtistProfileDTO dto = artistProfileMapper.toArtistProfileDTO(profile);
        dto.setAlbums(albumService.getAlbumsByArtistId(artistId));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createArtistProfileRequest(CreateArtistProfileRequestDTO dto) {
        ArtistProfile profile = new ArtistProfile();

        profile.setStageName(dto.stageName());
        profile.setBio(dto.bio());
        profile.setAvatarKey(dto.avatarKey());
        profile.setCoverKey(dto.coverKey());
        profile.setStatus(ArtistProfileStatus.PENDING);
        profile.setCreatedAt(LocalDateTime.now());

        return "Send request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveArtistProfileRequest(Long artistProfileId) {
        ArtistProfile profile = artistProfileRepo.findById(artistProfileId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        profile.setStatus(ArtistProfileStatus.VERIFIED);
        return "Approved artist profile request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rejectArtistProfileRequest(Long artistProfileId) {
        ArtistProfile profile = artistProfileRepo.findById(artistProfileId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        profile.setStatus(ArtistProfileStatus.REJECTED);
        return "Rejected artist profile request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateProfile(MemberUpdateProfileDTO dto) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        ArtistProfile profile = artistProfileRepo.findByMemberId(currentUserId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        profile.setAvatarKey(dto.avatarKey());
        profile.setStageName(dto.displayName());
        return "Profile updated successfully!";
    }
}
