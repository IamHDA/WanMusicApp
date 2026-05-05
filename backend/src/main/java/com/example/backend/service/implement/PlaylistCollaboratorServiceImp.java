package com.example.backend.service.implement;

import com.example.backend.Enum.CollaboratorPermission;
import com.example.backend.Enum.NotificationType;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.UpdateCollaboratorPermissionRequestDTO;
import com.example.backend.dto.UpdateCollaboratorRequestDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.PlaylistCollaborator;
import com.example.backend.mapper.MemberMapper;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaylistCollaboratorServiceImp implements PlaylistCollaboratorService {

    private final PlaylistRepository playlistRepo;
    private final MemberRepository memberRepo;
    private final MemberMapper memberMapper;
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
            Member member = memberRepo.findById(userId).orElseThrow(()-> new RuntimeException("Member not found!"));
            PlaylistCollaborator playlistCollaborator = new PlaylistCollaborator(playlist, member);
            playlistCollaborators.add(playlistCollaborator);

            CreateNotificationDTO notificationDTO = new CreateNotificationDTO();
            notificationDTO.setNotificationType(NotificationType.PLAYLIST_COLLABORATION);
            notificationDTO.setPlaylistId(dto.playlistId());
            notificationDTO.setSenderName(currentMemberName);
            notificationDTO.setTargetId(member.getId());

            createNotificationDTOS.add(notificationDTO);
        }

        playlistCollaboratorRepo.saveAllAndFlush(playlistCollaborators);

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
            PlaylistCollaborator playlistCollaborator = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(playlistId, userId)
                    .orElseThrow(()-> new RuntimeException("Collaborator not found!"));
            playlistCollaborators.add(playlistCollaborator);
        }

        playlistCollaboratorRepo.deleteAll(playlistCollaborators);

        return "Collaborators removed from playlist successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateCollaboratorPermissions(UpdateCollaboratorPermissionRequestDTO dto) {
        PlaylistCollaborator playlistCollaborator = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(dto.playlistId(), dto.collaboratorId())
                .orElseThrow(()-> new RuntimeException("Collaborator not found!"));

        for(String permission : dto.permissions()){
            CollaboratorPermission perm = CollaboratorPermission.valueOf(permission);
            playlistCollaborator.getCollabPermission().add(perm);
        }

        return "Add permissions successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String revokeCollaboratorPermissions(UpdateCollaboratorPermissionRequestDTO dto) {
        PlaylistCollaborator playlistCollaborator = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(dto.playlistId(), dto.collaboratorId())
                .orElseThrow(()-> new RuntimeException("Collaborator not found!"));

        Set<CollaboratorPermission> permissions = playlistCollaborator.getCollabPermission();

        for(String permission : dto.permissions()){
            CollaboratorPermission perm = CollaboratorPermission.valueOf(permission);
            permissions.remove(perm);
        }

        return "Remove permissions successfully!";
    }

    @Override
    public List<MemberProfilePreviewDTO> getPlaylistColabborators(Long playlistId) {
        return playlistCollaboratorRepo.findByPlaylist_Id(playlistId)
                .stream()
                .map(pc -> memberMapper.toPreviewDTO(pc.getCollaborator()))
                .toList();
    }

    @Override
    public List<String> getCollaboratorPermissions(Long collabId, Long playlistId) {
        return playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(playlistId, collabId)
                .stream()
                .flatMap(p -> p.getCollabPermission().stream().map(Enum::toString))
                .toList();
    }
}
