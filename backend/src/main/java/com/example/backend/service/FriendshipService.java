package com.example.backend.service;

import com.example.backend.dto.FriendStateDTO;
import com.example.backend.dto.PageResponse;

public interface FriendshipService {
    int countFriendByUserId(Long userId);
    PageResponse<FriendStateDTO> getFriendsState(int index, int size);
    String sendFriendRequest(Long friendId);
    String acceptFriendRequest(Long friendId);
    String rejectFriendRequest(Long friendId);
    String deleteFriendRequest(Long friendId);
    String deleteFriend(Long friendId);
    String getFriendshipStatus(Long currentUserId, Long friendId);
}
