package com.example.backend.service.implement;

import com.example.backend.dto.jam.*;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.JamMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.*;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.JamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JamSessionServiceImp implements JamSessionService {

    private final TrackRepository trackRepo;
    private final AlbumRepository albumRepo;
    private final PlaylistRepository playlistRepo;
    private final JamPlayerStateRepository jamPlayerStateRepo;
    private final JamSessionRepository jamSessionRepo;
    private final JamNotificationRepository jamNotificationRepo;
    private final JamParticipantRepository jamParticipantRepo;
    private final AuthenticationService authenticationService;
    private final MemberRepository memberRepo;
    private final JamMapper jamMapper;
    private final TrackMapper trackMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJamSessionContext(UpdateJamSessionContextRequestDTO dto) {
        try{
            JamSession jamSession = jamSessionRepo.findById(dto.jamId()).orElseThrow(()-> new RuntimeException("Jam session not found!"));
            if(dto.trackId() != null){
                Track track = trackRepo.findById(dto.trackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
                jamSession.setCurrentTrack(track);
            }
            if(dto.albumId() != null){
                Album album = albumRepo.findById(dto.albumId()).orElseThrow(()-> new RuntimeException("Album not found!"));
                jamSession.setContextAlbum(album);
            }
            if(dto.playlistId() != null){
                Playlist playlist = playlistRepo.findById(dto.playlistId()).orElseThrow(()-> new RuntimeException("Playlist not found!"));
                jamSession.setContextPlaylist(playlist);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JamDTO getJamSessionById(Long jamSessionId) {

        JamSession jamSession = jamSessionRepo.findById(jamSessionId).orElseThrow(()-> new RuntimeException("Jam session not found!"));
        JamDTO jamDTO = jamMapper.toJamDTO(jamSession);
        if(jamSession.getCurrentTrack() != null){
            TrackPreviewDTO trackPreviewDTO = trackMapper.toTrackPreviewDTO(jamSession.getCurrentTrack());
            JamTrackPreviewDTO jamTrackPreviewDTO = new JamTrackPreviewDTO(trackPreviewDTO);

            jamPlayerStateRepo.findByJamSessionId(jamSessionId).ifPresent(
                    jamPlayerState -> {
                        int currentSeekPosition = jamPlayerState.getCurrentSeekPosition();

                        if (jamPlayerState.isPlaying()) {
                            long elapsedMillis = Duration.between(
                                    jamPlayerState.getLastUpdated(),
                                    LocalDateTime.now()
                            ).toMillis();

                            currentSeekPosition += (int) (elapsedMillis * jamPlayerState.getPlaybackRate() / 1000.0);
                        }

                        jamTrackPreviewDTO.setCurrentSeekPosition(currentSeekPosition);
                        jamTrackPreviewDTO.setPlaying(jamPlayerState.isPlaying());
                    }
            );

            jamDTO.setJamTrack(jamTrackPreviewDTO);
        }

        return jamDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JamPreviewDTO createJamSession(CreateJamSessionRequestDTO dto) {
        JamSession jamSession = new JamSession();
        jamSession.setSessionCode(UUID.randomUUID().toString());
        jamSession.setOwner(memberRepo.findById(authenticationService.getCurrentMemberId()).get());
        jamSession.setSize(dto.getSize());
        jamSession.setPublic(!dto.isPrivate());

        try{
            jamSession = jamSessionRepo.save(jamSession);

            JamPlayerState jamPlayerState = new JamPlayerState();
            jamPlayerState.setJamSession(jamSession);
            jamPlayerState.setPlaying(false);
            jamPlayerState.setCurrentSeekPosition(0);
            jamPlayerState.setLastUpdated(LocalDateTime.now());

            jamSession.setPlayerState(jamPlayerState);

            jamPlayerStateRepo.save(jamPlayerState);
        } catch (Exception e){
            if(e.getMessage().contains("Duplicate entry")){
                throw new RuntimeException("You've already created a jam session!");
            }
        }

        JamPreviewDTO jamPreviewDTO = new JamPreviewDTO();
        jamPreviewDTO.setId(jamSession.getId());
        jamPreviewDTO.setCode(jamSession.getSessionCode());

        return jamPreviewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateJamSession(UpdateJamSessionRequestDTO dto) {
        JamSession jamSession = jamSessionRepo.findById(dto.jamSessionId()).get();
        jamSession.setPublic(dto.isPublic());
        jamSession.setSize(dto.size());

        return "Updated jam session successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteJamSession(Long jamSessionId) {
        JamSession jamSession = jamSessionRepo.findById(jamSessionId).get();

        jamNotificationRepo.deleteByJamSession_Id(jamSessionId);
        jamParticipantRepo.deleteBySession_Id(jamSessionId);
        jamSessionRepo.delete(jamSession);

        return "Deleted jam session successfully!";
    }
}
