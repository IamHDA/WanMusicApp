package com.example.backend.service;

public interface FriendshipService {
    int countFriendByUserId(Long userId);
    String addFriend(Long friendId);
    String deleteFriend(Long friendId);
}
