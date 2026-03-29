package com.example.backend.service.implement;

import com.example.backend.dto.AddInteractionRequestDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackStatisticDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.entity.UserInteraction;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.repository.UserInteractionRepository;
import com.example.backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImp implements StatisticService {

    private final TrackMapper trackMapper;
    private final UserInteractionRepository userInteractionRepo;
    private final AuthenticationServiceImp authenticationService;
    private final TrackRepository trackRepo;
    private final MemberRepository memberRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTrackStatistic(AddInteractionRequestDTO input) {
        Track track = trackRepo.findById(input.getTrackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        UserInteraction interaction = new UserInteraction();
        interaction.setTrack(track);
        interaction.setMember(member);
        interaction.setType(input.getType());
        interaction.setDuration(input.getDuration());

        return "Added to statistic successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTrackStatistic(UserInteraction input) {

        UserInteraction interaction = new UserInteraction();
        interaction.setTrack(input.getTrack());
        interaction.setMember(input.getMember());
        interaction.setType(input.getType());
        interaction.setTime(input.getTime());

    }

    @Override
    public TrackStatisticDTO getTrackStatistic(Long trackId) {
        return userInteractionRepo.findByTrackId(trackId);
    }

    @Override
    public List<TrackPreviewDTO> getTopTracks() {
        return userInteractionRepo.findTopTracksInMonth(LocalDateTime.now().getMonthValue())
                .stream()
                .map(trackMapper::toTrackPreviewDTO)
                .collect(Collectors.toList());
    }
}
