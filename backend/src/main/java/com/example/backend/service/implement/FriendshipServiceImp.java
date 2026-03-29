package com.example.backend.service.implement;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.EmbeddedId.FriendshipId;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.Member;
import com.example.backend.repository.FriendshipRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.FriendshipService;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImp implements FriendshipService {

    private final MemberRepository memberRepo;
    private final AuthenticationService authenticationService;
    private final FriendshipRepository friendshipRepo;
    private final NotificationService notificationService;

    @Override
    public int countFriendByUserId(Long userId) {
        return friendshipRepo.countFriendByUserId(userId);
    }

    @Override
    public String sendFriendRequest(Long friendId) {
        Friendship friendship = new Friendship();

        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        Member friend = memberRepo.findById(friendId).orElseThrow(()-> new RuntimeException("Member not found!"));

        FriendshipId friendshipId = new FriendshipId(currentMember.getId(), friend.getId());

        friendship.setId(friendshipId);
        friendship.setStatus(FriendStatus.PENDING);
        friendship.setMember(currentMember);
        friendship.setFriend(friend);
        friendship.setCreatedAt(LocalDateTime.now());

        friendshipRepo.save(friendship);

        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setFriendRequestId(friendshipId);
        dto.setSenderName(currentMember.getFullName());
        dto.setTargetId(friendId);
        dto.setNotificationType(NotificationType.FRIEND_REQUEST);

        notificationService.sendNotification(dto);

        return "Sent friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String acceptFriendRequest(Long friendId) {
        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentMember.getId(), friendId);

        friendship.setStatus(FriendStatus.ACCEPTED);

        return "Accepted friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String acceptFriendRequest(FriendshipId friendshipId) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow(()-> new RuntimeException("Friendship not found!"));
        friendship.setStatus(FriendStatus.ACCEPTED);

        return "Accepted friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteFriend(Long friendId) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        friendshipRepo.deleteByMemberIdAndFriendId(currentUserId, friendId);

        return "Deleted friend successfully!";
    }

    @Override
    public String getFriendshipStatus(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return "SELF";
        }
        Friendship friendship = friendshipRepo.findByMemberIdAndFriendId(currentUserId, targetUserId);
        if (friendship == null) {
            return "NONE";
        }
        if (friendship.getStatus() == FriendStatus.ACCEPTED) {
            return "ACCEPTED";
        }
        if (friendship.getStatus() == FriendStatus.PENDING) {
            if(friendship.getMember().getId().equals(currentUserId)) {
                return "PENDING_SENT";
            }else{
                return "PENDING_RECEIVED";
            }
        }
        return "NONE";
    }
}
