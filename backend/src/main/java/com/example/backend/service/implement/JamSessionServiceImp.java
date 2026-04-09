package com.example.backend.service.implement;

import com.example.backend.dto.jam.CreateJamSessionRequestDTO;
import com.example.backend.dto.jam.JamDTO;
import com.example.backend.dto.jam.UpdateJamSessionRequestDTO;
import com.example.backend.entity.JamSession;
import com.example.backend.repository.JamNotificationRepository;
import com.example.backend.repository.JamParticipantRepository;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.JamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JamSessionServiceImp implements JamSessionService {

    private final JamSessionRepository jamSessionRepo;
    private final JamNotificationRepository jamNotificationRepo;
    private final JamParticipantRepository jamParticipantRepo;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JamDTO createJamSession(CreateJamSessionRequestDTO dto) {
        JamSession jamSession = new JamSession();
        jamSession.setSessionCode(UUID.randomUUID().toString());
        jamSession.setOwner(memberRepo.findById(authenticationService.getCurrentMemberId()).get());
        jamSession.setSize(dto.getSize());
        jamSession.setPublic(!dto.isPrivate());

        try{
            jamSession = jamSessionRepo.save(jamSession);
        } catch (Exception e){
            if(e.getMessage().contains("Duplicate entry")){
                throw new RuntimeException("You've already created a jam session!");
            }
        }

        JamDTO jamDTO = new JamDTO();
        jamDTO.setId(jamSession.getId());
        jamDTO.setCode(jamSession.getSessionCode());

        return jamDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateJamSession(UpdateJamSessionRequestDTO dto) {
        JamSession jamSession = jamSessionRepo.findById(dto.jamSessionId()).get();
        jamSession.setPublic(dto.isPublic());
        jamSession.setSize(dto.size());

        return "Updated jam session successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteJamSession(Long jamSessionId) {
        JamSession jamSession = jamSessionRepo.findById(jamSessionId).get();

        jamNotificationRepo.deleteByJamSession_Id(jamSessionId);
        jamParticipantRepo.deleteBySession_Id(jamSessionId);
        jamSessionRepo.delete(jamSession);

        return "Deleted jam session successfully!";
    }
}
