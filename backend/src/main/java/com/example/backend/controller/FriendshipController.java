package com.example.backend.controller;

import com.example.backend.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/addFriend/{id}")
    public ResponseEntity<String> addFriend(@PathVariable Long id){
        return ResponseEntity.ok(friendshipService.sendFriendRequest(id));
    }

    @PutMapping("/acceptFriend/{friendId}")
    public ResponseEntity<String> acceptFriendRequestByFriendId(@PathVariable Long friendId){
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(friendId));
    }

    @PutMapping("/acceptFriend/{fiendshipId}")
    public ResponseEntity<String> acceptFriendRequestByFiendshipId(@PathVariable Long fiendshipId){
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(fiendshipId));
    }

    @DeleteMapping("/deleteFriend/{id}")
    public ResponseEntity<String> deleteFriend(@PathVariable Long id){
        return ResponseEntity.ok(friendshipService.deleteFriend(id));
    }

}
