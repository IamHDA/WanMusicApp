package com.example.backend.util;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.entity.Friendship;
import com.example.backend.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FriendUtil {

    private final FriendshipRepository friendshipRepo;

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

    public String getFriendshipStatus(Friendship friendship, Long currentUserId){
        if(Objects.isNull(friendship)) return "NONE";
        System.out.println("SDFDSF " + currentUserId);
        if(currentUserId.equals(friendship.getMember().getId()) && friendship.getStatus().equals(FriendStatus.PENDING))
            return "PENDING_SENT";
        if(currentUserId.equals(friendship.getFriend().getId()) && friendship.getStatus().equals(FriendStatus.PENDING))
            return "PENDING_RECEIVED";
        if(friendship.getStatus().equals(FriendStatus.ACCEPTED))
            return "ACCEPTED";
        return "NONE";
    }

}
