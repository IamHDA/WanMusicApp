package com.example.backend.service.implement;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.entity.Friendship;
import com.example.backend.entity.Member;
import com.example.backend.repository.FriendshipRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImp implements FriendshipService {

    private final MemberRepository memberRepo;
    private final AuthenticationService authenticationService;
    private final FriendshipRepository friendshipRepo;

    @Override
    public int countFriendByUserId(Long userId) {
        return friendshipRepo.countFriendByUserId(userId);
    }

    @Override
    public String addFriend(Long friendId) {
        Friendship friendship = new Friendship();

        Member currentMember = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        Member friend = memberRepo.findById(friendId).orElseThrow(()-> new RuntimeException("Member not found!"));

        friendship.setStatus(FriendStatus.PENDING);
        friendship.setMember(currentMember);
        friendship.setFriend(friend);

        friendshipRepo.save(friendship);

        return "Sent friend request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteFriend(Long friendId) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        friendshipRepo.deleteByMemberIdAndFriendId(currentUserId, friendId);

        return "Deleted friend successfully!";
    }
}
