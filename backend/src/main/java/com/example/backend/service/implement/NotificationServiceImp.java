package com.example.backend.service.implement;

import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.NotificationDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Notification;
import com.example.backend.mapper.NotificationMapper;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImp implements NotificationService {

    private final MemberRepository memberRepo;
    private final NotificationRepository notificationRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(CreateNotificationDTO dto) {
        Notification notification = new Notification();
        NotificationDTO notificationDTO = new NotificationDTO();
        Member receiver = memberRepo.findById(dto.getTargetId()).orElseThrow(()-> new RuntimeException("Member not found!"));

        if(dto.getNotificationType().equals(NotificationType.JAM_INVITE)){
            notification.setTitle("Jam Invite");
            notification.setMessage(dto.getSenderName() + " invited you to a jam session!");
            notification.setType(NotificationType.JAM_INVITE);

            notificationDTO.setType(NotificationType.JAM_INVITE);
            notificationDTO.setMessage(notification.getMessage());
            notificationDTO.setJamSessionId(dto.getJamSessionId());
        }else if(dto.getNotificationType().equals(NotificationType.FRIEND_REQUEST)){
            notification.setTitle("Friend Request");
            notification.setMessage(dto.getSenderName() + " sent you a friend request!");
            notification.setType(NotificationType.FRIEND_REQUEST);

            notificationDTO.setType(NotificationType.FRIEND_REQUEST);
            notificationDTO.setMessage(notification.getMessage());
            notificationDTO.setFriendRequestSenderId(dto.getFriendRequestSenderId());
        }else if(dto.getNotificationType().equals(NotificationType.PLAYLIST_COLLABORATION)){
            notification.setTitle("Playlist Collaboration");
            notification.setMessage(dto.getSenderName() + " invited you to collaborate on a playlist!");
            notification.setType(NotificationType.PLAYLIST_COLLABORATION);

            notificationDTO.setType(NotificationType.PLAYLIST_COLLABORATION);
            notificationDTO.setMessage(notification.getMessage());
            notificationDTO.setPlaylistId(dto.getPlaylistId());
        }else if(dto.getNotificationType().equals(NotificationType.SONG_RELEASING)){
            notification.setTitle("Song Releasing");
            notification.setMessage(dto.getSenderName() + " released a new song!");
            notification.setType(NotificationType.SONG_RELEASING);

            notificationDTO.setType(NotificationType.SONG_RELEASING);
            notificationDTO.setMessage(notification.getMessage());
            notificationDTO.setTrackId(dto.getTrackId());
        }

        notification.setReceiver(receiver);
        notification.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        notification.setRead(false);

        notificationRepo.save(notification);

        notificationDTO.setNotificationId(notification.getId());

        simpMessagingTemplate.convertAndSendToUser(String.valueOf(dto.getTargetId()), "/queue/notice", notificationDTO);
    }

    @Override
    public List<NotificationDTO> getNotifications(Long receiverId) {
        return notificationRepo.findByReceiver_IdOrderByCreatedAtDesc(receiverId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }
}
