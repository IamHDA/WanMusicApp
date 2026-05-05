package com.example.backend.controller;

import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.dto.CreateJamNotificationDTO;
import com.example.backend.dto.jam.JamNotificationDTO;
import com.example.backend.service.JamNotificationService;
import com.example.backend.service.UserInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user-interaction")
@RequiredArgsConstructor
public class UserInteractionController {

    private final JamNotificationService jamNotificationService;
    private final UserInteractionService userInteractionService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/jam/notification")
    public void handleJamNotification(CreateJamNotificationDTO request, Principal principal){

        if(principal == null)
            throw new IllegalStateException("Unauthenticated websocket user");

        JamNotificationDTO jamNotificationDTO = jamNotificationService.sendJamNotification(request, principal.getName());
        System.out.println(jamNotificationDTO.getInteractionType());
        simpMessagingTemplate.convertAndSend("/jam/notification/" + jamNotificationDTO.getJamSessionId(), jamNotificationDTO);
    }

    @PostMapping
    public ResponseEntity<String> saveInteraction(@RequestBody CreateInteractionRequestDTO dto){
        return ResponseEntity.ok(userInteractionService.addInteraction(dto));
    }

}
