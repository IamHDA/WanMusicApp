package com.example.backend.dto.authentication;

public record RegisterRequest (
        String email,
        String password,
        String displayName
){}
