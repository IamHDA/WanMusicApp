package com.example.backend.service;

import com.example.backend.dto.authentication.LogInRequest;
import com.example.backend.dto.authentication.RegisterRequest;
import com.example.backend.dto.authentication.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(LogInRequest request);
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse refreshToken(String refreshToken);
    Long getCurrentMemberId();
    String getCurrentMemberName();
}
