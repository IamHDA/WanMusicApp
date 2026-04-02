package com.example.backend.service.implement;

import com.example.backend.dto.SavePlayerStateRequestDTO;
import com.example.backend.entity.Album;
import com.example.backend.entity.Member;
import com.example.backend.entity.PlayerState;
import com.example.backend.entity.Playlist;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PlayerStateRepository;
import com.example.backend.repository.PlaylistRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerStateServiceImp implements PlayerStateService {

    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final AlbumRepository albumRepo;
    private final PlaylistRepository playlistRepo;
    private final PlayerStateRepository playerStateRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String savePlayerState(SavePlayerStateRequestDTO dto) {
        Long currentMemberId = authenticationService.getCurrentMemberId();

        PlayerState playerState = playerStateRepo
                .findByMemberId(currentMemberId)
                .orElseGet(() -> {
                    Member member = memberRepo.findById(currentMemberId)
                            .orElseThrow(() -> new RuntimeException("Member not found"));

                    PlayerState ps = new PlayerState();
                    ps.setMember(member);
                    return ps;
                });

        playerState.setCurrentSeekPosition(dto.currentSeekPosition());
        playerState.setLastUpdated(LocalDateTime.now());

        if (dto.playlistId() != null) {
            Playlist playlist = playlistRepo.findById(dto.playlistId()).orElse(null);
            playerState.setPlaylist(playlist);
            playerState.setAlbum(null);
        } else if (dto.albumId() != null) {
            Album album = albumRepo.findById(dto.albumId()).orElse(null);
            playerState.setAlbum(album);
            playerState.setPlaylist(null);
        }

        if (playerState.getId() == null) {
            playerStateRepo.save(playerState);
        }

        return "Player State saved successfully!";
    }
}
