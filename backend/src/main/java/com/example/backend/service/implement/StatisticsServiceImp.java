package com.example.backend.service.implement;

import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackStatisticDTO;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.UserInteractionRepository;
import com.example.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImp implements StatisticsService {

    private final TrackMapper trackMapper;
    private final UserInteractionRepository userInteractionRepo;

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
