package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.Enum.JamInteractionStatus;
import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateJamNotificationDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.jam.GetJamNotificationRequestDTO;
import com.example.backend.dto.jam.JamNotificationDTO;
import com.example.backend.entity.JamNotification;
import com.example.backend.entity.JamSession;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.mapper.JamNotificationMapper;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.JamNotificationRepository;
import com.example.backend.repository.JamSessionRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.CacheVersionService;
import com.example.backend.service.JamNotificationService;
import com.example.backend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JamNotificationServiceImp implements JamNotificationService {

    private final JamNotificationRepository jamNotificationRepo;
    private final TrackRepository trackRepo;
    private final JamSessionRepository jamSessionRepo;
    private final MemberRepository memberRepo;
    private final PageMapper pageMapper;
    private final JamNotificationMapper jamNotificationMapper;
    private final RedisService redisService;
    private final CacheVersionService cacheVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JamNotificationDTO sendJamNotification(CreateJamNotificationDTO request, String email) {
        Optional<JamSession> jamSession = jamSessionRepo.findById(request.getJamId());
        Member member = memberRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("Member not found!"));
        Track track = null;
        if(!request.getNotificationType().equals(NotificationType.JAM_JOIN) || !request.getNotificationType().equals(NotificationType.JAM_CLOSED))
            track = trackRepo.findById(request.getTrackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
        JamNotification jamNotification = new JamNotification();

        if(jamSession.isEmpty())
            throw new RuntimeException("Jam session not found!");

        boolean isOwner = member.getId().equals(jamSession.get().getOwner().getId());
        String message = "";

        if(request.getNotificationType().equals(NotificationType.JAM_INTERACTION)){
            if(request.getInteractionType().equals(InteractionType.PLAY)){
                if(isOwner) message = "Host started playing " + track.getTitle();
                else message = member.getFullName() + " wants to continue " + track.getTitle();
            }
            if(request.getInteractionType().equals(InteractionType.PICK)){
                if(isOwner) message = "Host played " + track.getTitle();
                else message = member.getFullName() + " wants to play " + track.getTitle();
            }
            else if(request.getInteractionType().equals(InteractionType.JUMP)){
                if(isOwner) message = "Host jumped to " + request.getDuration();
                else message = member.getFullName() + " wants to jump to " + formatSecondsToMMSS(request.getDuration());
            }
            else if(request.getInteractionType().equals(InteractionType.SKIP)){
                if(isOwner) message = "Host skipped this song";
                else message = member.getFullName() + " wants to skip this song";
            }
            else if(request.getInteractionType().equals(InteractionType.PAUSE)){
                if(isOwner) message = "Host paused the song";
                else message = member.getFullName() + " wants to pause the song";
            }
            else if(request.getInteractionType().equals(InteractionType.PREVIOUS)){
                if(isOwner) message = "Host went back to the previous song";
                else message = member.getFullName() + " wants to go back to the previous song";
            }
        }else if(request.getNotificationType().equals(NotificationType.JAM_JOIN)){
            message = member.getFullName() + " joined the jam";
        }else if(request.getNotificationType().equals(NotificationType.JAM_CLOSED)){
            message = "Jam session has been closed";
        }

        JamInteractionStatus status = null;

        if(isOwner){
            jamNotification.setStatus(null);
        } else {
            jamNotification.setStatus(JamInteractionStatus.PENDING);
            status = JamInteractionStatus.PENDING;
        }
        jamNotification.setMessage(message);
        jamNotification.setJamSession(jamSession.get());
        jamNotification.setType(request.getNotificationType());
        jamNotification.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        jamNotification = jamNotificationRepo.save(jamNotification);

        cacheVersionService.bumpJamNotificationVersion(jamSession.get().getId());

        JamNotificationDTO jamNotificationDTO = new JamNotificationDTO(
                jamSession.get().getId(),
                jamNotification.getId(),
                request.getTrackId(),
                member.getId(),
                jamNotification.getMessage(),
                jamNotification.getType(),
                request.getInteractionType(),
                status,
                request.getDuration(),
                jamNotification.getCreatedAt()
        );

        return jamNotificationDTO;
    }

    @Override
    public PageResponse<JamNotificationDTO> getJamNotifications(GetJamNotificationRequestDTO dto) {
        String key = "/jam/notification/" + "/" + dto.jamId() + cacheVersionService.getJamNotificationVersion(dto.jamId());

        PageResponse<JamNotificationDTO> response = null;

        if(redisService.hasKey(key)) response = (PageResponse<JamNotificationDTO>) redisService.get(key);
        else {

            response = pageMapper.toPageResponse(jamNotificationRepo.findByJamSessionId(
                    dto.jamId(),
                    PageRequest.of(dto.index() - 1, dto.size())
            ), jamNotificationMapper::toDTO);

            redisService.save(key, response, 60);
        }

        return response;
    }

    public static String formatSecondsToMMSS(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
