package com.example.backend.service.implement;

import com.example.backend.dto.playlistTrack.PlaylistTrackDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.*;
import com.example.backend.service.PlaylistTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistTrackServiceImp implements PlaylistTrackService {

    private final PlaylistCollaboratorRepository playlistCollaboratorRepo;
    private final PlaylistRepository playlistRepo;
    private final PlaylistTrackRepository playlistTrackRepo;
    private final TrackRepository trackRepo;
    private final AuthenticationServiceImp authenticationService;
    private final MemberRepository memberRepo;
    private final TrackMapper trackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTrackToPlaylist(PlaylistTrackDTO dto) {
        Long currentMemberId = authenticationService.getCurrentMemberId();

        List<PlaylistTrack> playlistTracks = new ArrayList<>();

        for(Long playlistId : dto.playlistId()){
            Optional<PlaylistCollaborator> collab = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(playlistId, currentMemberId);

            Playlist playlist = playlistRepo.findById(playlistId).orElseThrow(()-> new RuntimeException("Playlist not found!"));

            if(!Objects.equals(playlist.getOwner().getId(), currentMemberId) && collab.isEmpty())
                throw new RuntimeException("You are not a collaborator or owner of this playlist!");

            Track track = trackRepo.findById(dto.trackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
            Member member = memberRepo.findById(currentMemberId).orElseThrow(()-> new RuntimeException("Member not found!"));

            PlaylistTrack playlistTrack = new PlaylistTrack();
            playlistTrack.setAddedAt(LocalDateTime.now());
            playlistTrack.setPlaylist(playlist);
            playlistTrack.setTrack(track);
            playlistTrack.setAddedBy(member);

            playlistTracks.add(playlistTrack);
        }

        playlistTrackRepo.saveAllAndFlush(playlistTracks);

        return "Track added to playlists successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeTrackFromPlaylist(PlaylistTrackDTO dto) {
        Long currentMemberId = authenticationService.getCurrentMemberId();

        for(Long playlistId : dto.playlistId()){
            Optional<PlaylistCollaborator> collab = playlistCollaboratorRepo.findByPlaylist_IdAndCollaborator_Id(dto.playlistId().getFirst(), currentMemberId);

            if(collab.isEmpty())
                throw new RuntimeException("You are not a collaborator of this playlist!");

            playlistTrackRepo.deleteByPlaylistIdAndTrackId(playlistId, dto.trackId());
        }

        return "Track removed from playlists successfully!";
    }

    @Override
    public List<TrackPreviewDTO> getPlaylistTracks(Long playlistId) {
        return playlistTrackRepo.findAllTrackByPlaylistId(playlistId)
                .stream()
                .map(trackMapper::toTrackPreviewDTO)
                .toList();
    }

}
