package com.example.backend.service;

public interface FriendshipService {
    int countFriendByUserId(Long userId);
    String sendFriendRequest(Long friendId);
    String acceptFriendRequest(Long friendId);
    String rejectFriendRequest(Long friendId);
    String deleteFriendRequest(Long friendId);
    String deleteFriend(Long friendId);
    String getFriendshipStatus(Long currentUserId, Long friendId);
}
