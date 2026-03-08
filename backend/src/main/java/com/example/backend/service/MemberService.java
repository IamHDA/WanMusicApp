package com.example.backend.service;

import com.example.backend.dto.user.MemberProfileDTO;
import com.example.backend.dto.user.MemberUpdateProfileDTO;

public interface MemberService {
    String updateProfile(MemberUpdateProfileDTO dto);
    MemberProfileDTO getProfile(Long memberId);
}
