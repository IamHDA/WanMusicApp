package com.example.backend.controller;

import com.example.backend.dto.FriendStateDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/state")
    public ResponseEntity<PageResponse<FriendStateDTO>> getFriendshipState(
            @RequestParam("index") Integer index,
            @RequestParam("size") Integer size
    ){
        return ResponseEntity.ok(friendshipService.getFriendsState(index, size));
    }

    @PostMapping("/addFriend/{id}")
    public ResponseEntity<String> addFriend(@PathVariable Long id){
        return ResponseEntity.ok(friendshipService.sendFriendRequest(id));
    }

    @PutMapping("/acceptFriend/{friendId}")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long friendId){
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(friendId));
    }

    @PutMapping("/rejectFriend/{friendId}")
    public ResponseEntity<String> rejectFriendRequest(@PathVariable Long friendId){
        return ResponseEntity.ok(friendshipService.rejectFriendRequest(friendId));
    }

    @DeleteMapping("/deleteFriendRequest/{friendId}")
    public ResponseEntity<String> deleteFriendRequest(@PathVariable Long friendId){
        return ResponseEntity.ok(friendshipService.deleteFriendRequest(friendId));
    }

    @DeleteMapping("/deleteFriend/{id}")
    public ResponseEntity<String> deleteFriend(@PathVariable Long id){
        return ResponseEntity.ok(friendshipService.deleteFriend(id));
    }

}
