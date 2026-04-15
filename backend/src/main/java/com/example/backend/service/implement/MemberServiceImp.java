package com.example.backend.service.implement;

import com.example.backend.dto.user.AccountSettingsDTO;
import com.example.backend.dto.user.MemberProfileDTO;
import com.example.backend.dto.user.MyProfileDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.entity.Member;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.*;
import com.example.backend.util.FriendUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImp implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberRepository memberRepo;
    private final ArtistProfileRepository artistProfileRepo;
    private final AuthenticationService authenticationService;
    private final FollowerService followerService;
    private final PlaylistService playlistService;
    private final FriendshipService friendshipService;
    private final FriendUtil friendUtil;
    private final S3StorageService s3StorageService;
    private final CacheVersionService cacheVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateProfile(MemberUpdateProfileDTO dto) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        Member currentMember = memberRepo.findById(currentUserId).orElseThrow(()-> new RuntimeException("Member not found!")) ;
        // 1. Chỉ đè Avatar nếu Frontend Gửi lên có ảnh mới (Khác NULL)
        if (dto.avatarKey() != null && !dto.avatarKey().trim().isEmpty()) {
            String oldKey = currentMember.getAvatarKey();
            s3StorageService.deleteFile(oldKey, "avatars");
            currentMember.setAvatarKey(dto.avatarKey());
        }
        // 2. Chỉ đổi Tên nếu Frontend Gửi Tên mới
        if (dto.displayName() != null && !dto.displayName().trim().isEmpty()) {
            currentMember.setFullName(dto.displayName());
        }

        cacheVersionService.bumpMemberVersion();

        return "Profile updated successfully!";
    }

    @Override
    public MemberProfileDTO getProfile(Long memberId) {
        Member member = memberRepo.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found!"));

        MemberProfileDTO memberProfileDTO = memberMapper.toProfileDTO(member);

        memberProfileDTO.setSubscriptionType(member.getSubscriptionType().name());

        Long currentUserId = authenticationService.getCurrentMemberId();
        String status = friendUtil.getFriendshipStatus(currentUserId, memberId);
        memberProfileDTO.setFriendStatus(status);

        if(currentUserId.equals(memberId)){
            Optional<ArtistProfile> artistProfile = artistProfileRepo.findByMemberId(memberId);
            if(!artistProfile.isPresent())
                memberProfileDTO.setArtistProfileStatus("NONE");
            else
                memberProfileDTO.setArtistProfileStatus(artistProfile.get().getStatus().name());
        }

        memberProfileDTO.setFollowedArtistCount(followerService.countFollowedArtistByUserId(memberId));
        memberProfileDTO.setFriendCount(friendshipService.countFriendByUserId(memberId));
        memberProfileDTO.setPlaylistCount(playlistService.countPlaylistsByOwnerId(memberId));

        return memberProfileDTO;
    }

    @Override
    public MyProfileDTO getMyProfile() {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Member member = memberRepo.findById(currentUserId).orElseThrow(() -> new RuntimeException("Member not found!"));

        MyProfileDTO myProfileDTO = new MyProfileDTO();
        myProfileDTO.setId(member.getId());
        myProfileDTO.setDisplayName(member.getFullName());
        myProfileDTO.setAvatarUrl(s3StorageService.getGetPresignedUrl(member.getAvatarKey(), "avatars"));
        myProfileDTO.setSubscriptionType(member.getSubscriptionType().name());

        artistProfileRepo.findByMemberId(currentUserId).ifPresent(artistProfile -> {
            myProfileDTO.setArtistStatus(artistProfile.getStatus().name());
            myProfileDTO.setArtistStageName(artistProfile.getStageName());
            myProfileDTO.setArtistBio(artistProfile.getBio());
            if (artistProfile.getAvatarKey() != null) {
                myProfileDTO.setArtistAvatarUrl(s3StorageService.getGetPresignedUrl(artistProfile.getAvatarKey(), "avatars"));
            }
            if (artistProfile.getCoverKey() != null) {
                myProfileDTO.setArtistCoverUrl(s3StorageService.getGetPresignedUrl(artistProfile.getCoverKey(), "covers"));
            }
        });

        if (myProfileDTO.getArtistStatus() == null) {
            myProfileDTO.setArtistStatus("NONE");
        }

        myProfileDTO.setFriendStatus("SELF");
        myProfileDTO.setFollowedArtistCount(followerService.countFollowedArtistByUserId(currentUserId));
        myProfileDTO.setFriendCount(friendshipService.countFriendByUserId(currentUserId));
        myProfileDTO.setPlaylistCount(playlistService.countPlaylistsByOwnerId(currentUserId));

        // Nếu muốn chèn Playlist Preview, có thể map từ playlistService ở đây
        // myProfileDTO.setPlaylists(playlistService.getPreviewPlaylists(currentUserId));

        return myProfileDTO;
    }

    @Override
    public AccountSettingsDTO getAccountSettings() {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Member member = memberRepo.findById(currentUserId).orElseThrow(() -> new RuntimeException("Member not found!"));

        AccountSettingsDTO accountSettingsDTO = new AccountSettingsDTO();
        accountSettingsDTO.setId(member.getId());
        accountSettingsDTO.setDisplayName(member.getFullName());
        accountSettingsDTO.setEmail(member.getEmail());
        accountSettingsDTO.setAvatarUrl(member.getAvatarKey());
        accountSettingsDTO.setSubscriptionType(member.getSubscriptionType().name());
        return accountSettingsDTO;
    }
}
