package com.example.backend.service.implement;

import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.UpdateCollaboratorRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.PlaylistCollaborator;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlaylistCollaboratorRepository;
import com.example.backend.repository.PlaylistRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.service.PlaylistCollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistCollaboratorServiceImp implements PlaylistCollaboratorService {

    private final PlaylistRepository playlistRepo;
    private final MemberRepository memberRepo;
    private final PlaylistCollaboratorRepository playlistCollaboratorRepo;
    private final NotificationService notificationService;
    private final AuthenticationServiceImp authenticationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addCollaboratorToPlaylist(UpdateCollaboratorRequestDTO dto) {
        List<PlaylistCollaborator> playlistCollaborators = new ArrayList<>();
        List<CreateNotificationDTO> createNotificationDTOS = new ArrayList<>();
        Playlist playlist = playlistRepo.findById(dto.playlistId()).orElseThrow(()-> new RuntimeException("Playlist not found!"));
        String currentMemberName = authenticationService.getCurrentMemberName();

        for(Long userId : dto.userIds()) {
            PlaylistCollaborator playlistCollaborator = new PlaylistCollaborator();
            Member member = memberRepo.findById(userId).orElseThrow(()-> new RuntimeException("Member not found!"));

            playlistCollaborator.setPlaylist(playlist);
            playlistCollaborator.setCollaborator(member);

            CreateNotificationDTO notificationDTO = new CreateNotificationDTO();
            notificationDTO.setNotificationType(NotificationType.PLAYLIST_COLLABORATION);
            notificationDTO.setPlaylistId(dto.playlistId());
            notificationDTO.setSenderName(currentMemberName);
            notificationDTO.setTargetId(member.getId());

            createNotificationDTOS.add(notificationDTO);
        }

        playlistCollaboratorRepo.saveAll(playlistCollaborators);

        for(CreateNotificationDTO notificationDTO : createNotificationDTOS)
            notificationService.sendNotification(notificationDTO);

        return "Collaborators added to playlist successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeCollaboratorFromPlaylist(UpdateCollaboratorRequestDTO dto) {
        Long playlistId = dto.playlistId();
        List<PlaylistCollaborator> playlistCollaborators = new ArrayList<>();

        for(Long userId : dto.userIds()) {
            PlaylistCollaborator playlistCollaborator = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(playlistId, userId).get();
            playlistCollaborators.add(playlistCollaborator);
        }

        playlistCollaboratorRepo.deleteAll(playlistCollaborators);

        return "Collaborators removed from playlist successfully!";
    }
}
