package com.example.backend.service.implement;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.entity.Tag;
import com.example.backend.entity.Track;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.PlayerStateRepository;
import com.example.backend.repository.TagRepository;
import com.example.backend.repository.TrackTagRepository;
import com.example.backend.repository.UserInteractionRepository;
import com.example.backend.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImp implements PlayerService {

    private final AuthenticationServiceImp authenticationService;
    private final PlayerStateRepository playerStateRepo;
    private final TrackTagRepository trackTagRepo;
    private final TrackMapper trackMapper;

    @Override
    public PageResponse<TrackPreviewDTO> generateQueue(int index) {
        Long currentMemberId = authenticationService.getCurrentMemberId();
        Track currentTrack = playerStateRepo.findByMemberId(currentMemberId).get().getTrack();

        List<Long> currentTrackTagIds = trackTagRepo.findByTrack_Id(currentTrack.getId())
                .stream()
                .map(tt -> tt.getTag().getId())
                .toList();

        Pageable pageable = PageRequest.of(index, 10);

        List<TrackPreviewDTO> recommendTracks = trackTagRepo.findByTag_IdIn(currentTrackTagIds, pageable)
                .stream()
                .map(tt -> trackMapper.toTrackPreviewDTO(tt.getTrack()))
                .toList();

        return new PageResponse<>(recommendTracks, index, 10);
    }
}
