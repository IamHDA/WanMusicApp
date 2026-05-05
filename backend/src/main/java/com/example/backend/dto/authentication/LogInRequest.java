package com.example.backend.dto.authentication;

public record LogInRequest (
        String email,
        String password
){}
