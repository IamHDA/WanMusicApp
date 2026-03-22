package com.example.backend.service.implement;

import com.example.backend.dto.jam.AcceptInvitationRequestDTO;
import com.example.backend.entity.EmbeddedId.JamParticipantId;
import com.example.backend.entity.JamParticipant;
import com.example.backend.entity.JamSession;
import com.example.backend.entity.Member;
import com.example.backend.repository.JamParticipantRepository;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.JamParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JamParticipantServiceImp implements JamParticipantService {

    private final JamSessionRepository jamSessionRepo;
    private final JamParticipantRepository jamParticipantRepo;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String joinJam(AcceptInvitationRequestDTO request) {
        JamParticipant participant = new JamParticipant();
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).get();
        JamSession jamSession = jamSessionRepo.findById(request.jamSessionId()).get();
        participant.setParticipant(member);
        participant.setSession(jamSession);

        participant.setId(new JamParticipantId(request.jamSessionId(), member.getId()));
        jamParticipantRepo.save(participant);

        return "Joined jam session successfully!";
    }
}
