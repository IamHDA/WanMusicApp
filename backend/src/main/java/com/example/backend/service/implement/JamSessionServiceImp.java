package com.example.backend.service.implement;

import com.example.backend.dto.jam.CreateJamSessionRequestDTO;
import com.example.backend.entity.JamSession;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.JamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JamSessionServiceImp implements JamSessionService {

    private final JamSessionRepository jamSessionRepo;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createJamSession(CreateJamSessionRequestDTO dto) {
        JamSession jamSession = new JamSession();
        jamSession.setSessionCode(UUID.randomUUID().toString());
        jamSession.setOwner(memberRepo.findById(authenticationService.getCurrentMemberId()).get());
        jamSession.setSize(dto.getSize());
        jamSession.setPublic(dto.isPrivate());
        jamSessionRepo.save(jamSession);


        return "Created jam session successfully!";
    }
}
