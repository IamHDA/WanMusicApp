package com.example.backend.controller;

import com.example.backend.dto.authentication.AuthenticationResponse;
import com.example.backend.dto.authentication.LogInRequest;
import com.example.backend.dto.authentication.RegisterRequest;
import com.example.backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LogInRequest request){
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new AuthenticationResponse(null, null, null, "Invalid Token")
            );
        }

        String refreshToken = token.substring(7);

        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

}
