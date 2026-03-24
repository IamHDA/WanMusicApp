package com.example.backend.service;

public interface FriendshipService {
    int countFriendByUserId(Long userId);
    String addFriend(Long friendId);
    String acceptFriend(Long friendId);
    String deleteFriend(Long friendId);
    String getFriendshipStatus(Long currentUserId, Long targetUserId);
}
