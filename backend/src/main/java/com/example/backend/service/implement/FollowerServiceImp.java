package com.example.backend.service.implement;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.entity.Follower;
import com.example.backend.entity.Member;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.FollowerRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.CacheVersionService;
import com.example.backend.service.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowerServiceImp implements FollowerService {

    private final FollowerRepository followerRepo;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final ArtistProfileRepository artistProfileRepo;
    private final MemberMapper memberMapper;
    private final PageMapper pageMapper;
    private final CacheVersionService cacheVersionService;

    @Override
    public int countFollowedArtistByUserId(Long userId) {
        return followerRepo.countByFollower_Id(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String followArtist(Long artistId) {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Member member = memberRepo.findById(currentUserId).orElseThrow(()-> new RuntimeException("Member not found!"));
        ArtistProfile artist = artistProfileRepo.findById(artistId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        followerRepo.save(new Follower(member, artist));


        cacheVersionService.bumpArtistVersion();

        return "Followed artist successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unfollowArtist(Long artistId) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        cacheVersionService.bumpArtistVersion();

        followerRepo.deleteByFollower_IdAndArtist_Id(currentUserId, artistId);

        return "Unfollowed artist successfully!";
    }

    @Override
    public PageResponse<MemberProfilePreviewDTO> getArtistFollower(Long artistId, int page, int size) {
        Long currentUserId = authenticationService.getCurrentMemberId();
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Follower> followerPage =
                followerRepo.findByFollower_IdAndArtist_Id(currentUserId, artistId, pageable);

        Page<Member> followers = followerPage.map(Follower::getFollower);

        return pageMapper.toPageResponse(followers, memberMapper::toPreviewDTO);
    }
}
