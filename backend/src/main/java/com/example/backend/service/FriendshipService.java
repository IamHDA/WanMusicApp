package com.example.backend.service;

import com.example.backend.entity.EmbeddedId.FriendshipId;

public interface FriendshipService {
    int countFriendByUserId(Long userId);
    String sendFriendRequest(Long friendId);
    String acceptFriendRequest(Long friendId);
    String acceptFriendRequest(FriendshipId friendshipId);
    String deleteFriend(Long friendId);
    String getFriendshipStatus(Long currentUserId, Long targetUserId);
}
