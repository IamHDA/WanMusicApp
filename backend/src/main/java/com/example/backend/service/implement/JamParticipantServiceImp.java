package com.example.backend.service.implement;

import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateJamInvitationRequestDTO;
import com.example.backend.dto.CreateJamNotificationDTO;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.jam.JamParticipantRequestDTO;
import com.example.backend.entity.EmbeddedId.JamParticipantId;
import com.example.backend.entity.JamParticipant;
import com.example.backend.entity.JamSession;
import com.example.backend.entity.Member;
import com.example.backend.repository.JamParticipantRepository;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.JamNotificationService;
import com.example.backend.service.JamParticipantService;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JamParticipantServiceImp implements JamParticipantService {

    private final JamSessionRepository jamSessionRepo;
    private final JamParticipantRepository jamParticipantRepo;
    private final JamNotificationService jamNotificationService;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long joinJamById(JamParticipantRequestDTO request) {
        JamParticipant participant = new JamParticipant();
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).get();
        JamSession jamSession = jamSessionRepo.findById(request.jamSessionId()).get();
        participant.setParticipant(member);
        participant.setSession(jamSession);

        participant.setId(new JamParticipantId(request.jamSessionId(), member.getId()));

        jamParticipantRepo.save(participant);

        CreateJamNotificationDTO dto = new CreateJamNotificationDTO();
        dto.setJamJd(request.jamSessionId());
        dto.setNotificationType(NotificationType.JAM_JOIN);
        dto.setUsername(member.getFullName());

        jamNotificationService.sendJamNotification(dto);

        return jamSession.getId();
    }

    @Override
    public String inviteMember(CreateJamInvitationRequestDTO request) {
        for(Long memberId: request.memberIds()){
            CreateNotificationDTO dto = new CreateNotificationDTO();
            dto.setSenderName(authenticationService.getCurrentMemberName());
            dto.setJamSessionId(request.jamSessionId());
            dto.setTargetId(memberId);
            dto.setNotificationType(NotificationType.JAM_INVITE);

            notificationService.sendNotification(dto);
        }

        return "Members invited successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String leaveJam(JamParticipantRequestDTO request) {
        Long currentMemberId = authenticationService.getCurrentMemberId();

        jamParticipantRepo.deleteBySession_IdAndParticipant_Id(request.jamSessionId(), currentMemberId);

        return "Leave jam successfully!";
    }

    @Override
    public Long joinJamByCode(JamParticipantRequestDTO requestDTO) {
        JamParticipant participant = new JamParticipant();
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).get();
        JamSession jamSession = jamSessionRepo.findBySessionCode(requestDTO.jamSessionCode());
        participant.setParticipant(member);
        participant.setSession(jamSession);

        participant.setId(new JamParticipantId(jamSession.getId(), member.getId()));

        jamParticipantRepo.save(participant);

        CreateJamNotificationDTO dto = new CreateJamNotificationDTO();
        dto.setJamJd( jamSession.getId());
        dto.setNotificationType(NotificationType.JAM_JOIN);
        dto.setUsername(member.getFullName());

        jamNotificationService.sendJamNotification(dto);

        return jamSession.getId();
    }
}
