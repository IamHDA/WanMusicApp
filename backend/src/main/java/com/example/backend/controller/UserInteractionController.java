package com.example.backend.controller;

import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.service.UserInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-interaction")
@RequiredArgsConstructor
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping
    public ResponseEntity<String> saveInteraction(CreateInteractionRequestDTO dto){
        return ResponseEntity.ok(userInteractionService.addInteraction(dto));
    }

}
