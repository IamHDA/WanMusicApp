package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.entity.TrackFavourite;
import com.example.backend.entity.UserInteraction;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackFavouriteRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.RedisService;
import com.example.backend.service.TrackFavouriteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackFavouriteServiceImp implements TrackFavouriteService {

    private final TrackRepository trackRepo;
    private final MemberRepository memberRepo;
    private final AuthenticationServiceImp authenticationService;
    private final TrackFavouriteRepository trackFavouriteRepo;
    private final StatisticServiceImp statisticService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TrackMapper trackMapper;

    @Override
    public List<TrackPreviewDTO> getFavouriteTracks(boolean updated) {
        Long currentMemberId = authenticationService.getCurrentMemberId();
        String key = "/favourite/tracks/" + currentMemberId;
        if(!updated && redisService.hasKey(key))
            return objectMapper.convertValue(redisService.get(key), new TypeReference<List<TrackPreviewDTO>>() {});

        List<TrackPreviewDTO> tracks = trackFavouriteRepo.findByMember_Id(currentMemberId)
                .stream()
                .map(tf -> trackMapper.toTrackPreviewDTO(tf.getTrack()))
                .toList();

        redisService.save(key, tracks, 60);

        return tracks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTrackToFavourite(Long trackId) {
        Track track = trackRepo.findById(trackId).orElseThrow(()-> new RuntimeException("Track not found!"));
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        UserInteraction userInteraction = new UserInteraction();
        userInteraction.setMember(member);
        userInteraction.setTrack(track);
        userInteraction.setType(InteractionType.SAVED);
        userInteraction.setTime(LocalDateTime.now());
        userInteraction.setDuration(0);

        trackFavouriteRepo.save(new TrackFavourite(member, track));
        statisticService.addTrackStatistic(userInteraction);

        return "Added to favourite successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeTrackFromFavourite(Long trackId) {
        Long currentMemberId = authenticationService.getCurrentMemberId();
        TrackFavourite trackFavourite = trackFavouriteRepo.findByMember_IdAndTrack_Id(currentMemberId, trackId);

        if(trackFavourite != null)
            trackFavouriteRepo.delete(trackFavourite);
        else throw new RuntimeException("Track not found in favourite!");

        return "Track removed from favourite successfully!";
    }

}
