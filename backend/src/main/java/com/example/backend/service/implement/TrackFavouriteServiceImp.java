package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.entity.TrackFavourite;
import com.example.backend.entity.UserInteraction;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackFavouriteRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.TrackFavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrackFavouriteServiceImp implements TrackFavouriteService {

    private final TrackRepository trackRepo;
    private final MemberRepository memberRepo;
    private final AuthenticationServiceImp authenticationService;
    private final TrackFavouriteRepository trackFavouriteRepo;
    private final StatisticServiceImp statisticService;

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
