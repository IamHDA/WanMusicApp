package com.example.backend.service.implement;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.Enum.NotificationType;
import com.example.backend.Enum.UserStatus;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.FriendStateDTO;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.entity.EmbeddedId.FriendshipId;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.Member;
import com.example.backend.entity.PlayerState;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.FriendshipRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlayerStateRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.CacheVersionService;
import com.example.backend.service.FriendshipService;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImp implements FriendshipService {

    private final MemberRepository memberRepo;
    private final TrackMapper trackMapper;
    private final MemberMapper memberMapper;
    private final AuthenticationService authenticationService;
    private final PlayerStateRepository playerStateRepo;
    private final FriendshipRepository friendshipRepo;
    private final NotificationService notificationService;
    private final CacheVersionService cacheVersionService;

    @Override
    public int countFriendByUserId(Long userId) {
        return friendshipRepo.countFriendByUserId(userId);
    }

    @Override
    public PageResponse<FriendStateDTO> getFriendsState(int index, int size) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        Page<Friendship> friendships = friendshipRepo.findByMemberId(currentUserId, PageRequest.of(index - 1, size));

        Page<FriendStateDTO> result = friendships.map(fs -> {
            FriendStateDTO dto = new FriendStateDTO();
            Member friend = fs.getFriend();
            PlayerState ps = playerStateRepo
                    .findByMemberId(friend.getId())
                    .orElse(new PlayerState());

            dto.setCurrentTrack(trackMapper.toTrackPreviewDTO(ps.getTrack()));
            dto.setStatus(friend.getStatus());
            dto.setMember(memberMapper.toPreviewDTO(friend));

            return dto;
        });

        return new PageResponse<>(result.getContent(), index, friendships.getTotalPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendFriendRequest(Long friendId) {
        Friendship friendship = new Friendship();

        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        Member friend = memberRepo.findById(friendId).orElseThrow(()-> new RuntimeException("Member not found!"));

        FriendshipId friendshipId = new FriendshipId(currentMember.getId(), friend.getId());

        friendship.setId(friendshipId);
        friendship.setStatus(FriendStatus.PENDING);
        friendship.setMember(currentMember);
        friendship.setFriend(friend);
        friendship.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        friendshipRepo.save(friendship);

        cacheVersionService.bumpFriendVersion();

        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setFriendRequestSenderId(currentMember.getId());
        dto.setSenderName(currentMember.getFullName());
        dto.setTargetId(friendId);
        dto.setNotificationType(NotificationType.FRIEND_REQUEST);

        notificationService.sendNotification(dto);

        return "Sent friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rejectFriendRequest(Long friendId) {
        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        cacheVersionService.bumpFriendVersion();

        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentMember.getId(), friendId);

        friendship.setStatus(FriendStatus.ACCEPTED);

        return "Rejected friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteFriendRequest(Long friendId) {
        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentMember.getId(), friendId);

        cacheVersionService.bumpFriendVersion();

        if(friendship.getStatus() != FriendStatus.PENDING)
            throw new RuntimeException("Cannot delete friend request which is not pending!");

        System.out.println("Receiver: " + friendship.getId().getFriendId());
        System.out.println("Member: " + currentMember.getId());

        friendshipRepo.delete(friendship);

        return "Deleted  friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String acceptFriendRequest(Long friendId) {
        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        cacheVersionService.bumpFriendVersion();

        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentMember.getId(), friendId);

        friendship.setStatus(FriendStatus.ACCEPTED);

        return "Accepted friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteFriend(Long friendId) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        cacheVersionService.bumpFriendVersion();

        friendshipRepo.deleteByMemberIdAndFriendId(currentUserId, friendId);

        return "Deleted friend successfully!";
    }

    public String getFriendshipStatus(Long currentUserId, Long friendId) {
        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentUserId, friendId);

        if(friendship == null)
            return "NONE";

        if(Objects.equals(friendship.getMember().getId(), currentUserId) && friendship.getStatus().equals(FriendStatus.PENDING))
            return "PENDING_SENT";
        if(Objects.equals(friendship.getFriend().getId(), currentUserId) && friendship.getStatus().equals(FriendStatus.PENDING))
            return "PENDING_RECEIVED";
        if(friendship.getStatus().equals(FriendStatus.ACCEPTED))
            return "FRIEND";
        return "NONE";
    }

}
