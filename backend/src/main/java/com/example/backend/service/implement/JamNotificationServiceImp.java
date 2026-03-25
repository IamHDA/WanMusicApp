package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateJamNotificationRequestDTO;
import com.example.backend.dto.JamNotificationDTO;
import com.example.backend.entity.JamNotification;
import com.example.backend.entity.JamSession;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.repository.JamNotificationRepository;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.JamNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JamNotificationServiceImp implements JamNotificationService {

    private final AuthenticationServiceImp authenticationService;
    private final JamNotificationRepository jamNotificationRepo;
    private final TrackRepository trackRepo;
    private final JamSessionRepository jamSessionRepo;
    private final MemberRepository memberRepo;

    @Override
    public JamNotificationDTO sendJamNotification(CreateJamNotificationRequestDTO request) {
        Optional<JamSession> jamSession = jamSessionRepo.findById(request.jamId());
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        Track track = trackRepo.findById(request.trackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
        JamNotification jamNotification = new JamNotification();

        if(request.notificationType().equals(NotificationType.JAM_INTERACTION)){
            if(request.interactionType().equals(InteractionType.PLAY))
                jamNotification.setMessage(member.getFullName() + " wants to play " + track.getTitle());
            else if(request.interactionType().equals(InteractionType.JUMP))
                jamNotification.setMessage(member.getFullName() + " wants to jump to " + formatSecondsToMMSS(request.duration()));
            else if(request.interactionType().equals(InteractionType.SKIP))
                jamNotification.setMessage(member.getFullName() + " wants to skip this song");
            else if(request.interactionType().equals(InteractionType.PAUSE))
                jamNotification.setMessage(member.getFullName() + " wants to pause the song");
            else if(request.interactionType().equals(InteractionType.PREVIOUS))
                jamNotification.setMessage(member.getFullName() + " wants to go back to the previous song");
        }else if(request.notificationType().equals(NotificationType.JAM_JOIN))
            jamNotification.setMessage(member.getFullName() + " joined the jam");

        if(jamSession.isEmpty())
            throw new RuntimeException("Jam session not found!");

        jamNotification.setJamSession(jamSession.get());
        jamNotification.setType(request.notificationType());
        jamNotification.setCreatedAt(LocalDateTime.now());

        jamNotification = jamNotificationRepo.save(jamNotification);

        return new JamNotificationDTO(
                jamNotification.getId(),
                jamNotification.getMessage(),
                jamNotification.getType(),
                jamNotification.getCreatedAt()
        );
    }

    private String formatSecondsToMMSS(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        // %02d đảm bảo luôn có 2 chữ số, nếu nhỏ hơn 10 sẽ thêm số 0 đằng trước
        return String.format("%02d:%02d", minutes, seconds);
    }
}
