package com.example.backend.service.implement;

import com.example.backend.dto.playlist.PlaylistDTO;
import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.dto.playlist.UpdatePlaylistDetailDTO;
import com.example.backend.entity.Playlist;
import com.example.backend.entity.PlaylistCollaborator;
import com.example.backend.mapper.PlaylistMapper;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlaylistCollaboratorRepository;
import com.example.backend.repository.PlaylistRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImp implements PlaylistService {

    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final PlaylistMapper playlistMapper;
    private final PlaylistRepository playlistRepo;
    private final PlaylistCollaboratorRepository playlistCollaboratorRepo;


    @Override
    public PlaylistDTO getPlaylistById(Long playlistId) {
        Playlist foundPlaylist = playlistRepo.findById(playlistId).orElseThrow(()-> new RuntimeException("Playlist not found!"));
        return playlistMapper.toPlaylistDTO(foundPlaylist);
    }

    @Override
    public List<PlaylistPreviewDTO> getPlaylistsByOwnerId(Long ownerId) {
        return playlistMapper.toPlaylistPreviewDTOList(playlistRepo.findAllByOwnerId(ownerId).orElseThrow(()-> new RuntimeException("Playlist not found!")));
    }

    @Override
    public int countPlaylistsByOwnerId(Long ownerId) {
        return playlistRepo.countByOwnerId(ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlaylist(String name) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        Playlist playlist = new Playlist();
        playlist.setTitle(name);
        playlist.setThumbnailKey("default.png");
        playlist.setOwner(memberRepo.findById(currentUserId).orElseThrow(()-> new RuntimeException("Member not found!")));

        playlistRepo.save(playlist);

        return playlist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updatePlaylistDetail(UpdatePlaylistDetailDTO dto) {
        Optional<PlaylistCollaborator> playlistCollab = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(
                dto.id(), authenticationService.getCurrentMemberId());
        if(playlistCollab.isEmpty())
            throw new RuntimeException("You are not a collaborator of this playlist!");
        Playlist playlist = playlistRepo.findById(dto.id()).orElseThrow(()-> new RuntimeException("Playlist not found!"));

        playlist.setTitle(dto.name());
        playlist.setDescription(dto.description());
        playlist.setThumbnailKey(dto.thumbnailKey());

        return "Playlist detail updated successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deletePlaylist(Long playlistId) {
        playlistRepo.deleteById(playlistId);
        return "Playlist deleted successfully!";
    }
}
