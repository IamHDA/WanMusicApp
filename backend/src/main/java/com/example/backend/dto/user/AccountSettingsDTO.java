package com.example.backend.dto.user;

import lombok.Data;

@Data
public class AccountSettingsDTO {
    private Long id;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String subscriptionType;
}
