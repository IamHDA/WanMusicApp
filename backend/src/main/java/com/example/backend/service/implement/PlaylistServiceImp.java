package com.example.backend.service.implement;

import com.example.backend.dto.playlist.PlaylistPreviewDTO;
import com.example.backend.entity.Playlist;
import com.example.backend.mapper.PlaylistMapper;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlaylistRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.MemberService;
import com.example.backend.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImp implements PlaylistService {

    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final PlaylistMapper playlistMapper;
    private final PlaylistRepository playlistRepo;

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
    public String createPlaylist(String name) {
        Long currentUserId = authenticationService.getCurrentMemberId();

        Playlist playlist = new Playlist();
        playlist.setTitle(name);
        playlist.setOwner(memberRepo.findById(currentUserId).orElseThrow(()-> new RuntimeException("Member not found!")));

        playlistRepo.save(playlist);
        return "Playlist created successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deletePlaylist(Long playlistId) {
        playlistRepo.deleteById(playlistId);
        return "Playlist deleted successfully!";
    }
}
