package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.entity.UserInteraction;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.repository.UserInteractionRepository;
import com.example.backend.service.UserInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserInteractionServiceImp implements UserInteractionService {

    private final UserInteractionRepository userInteractionRepo;
    private final AuthenticationServiceImp authenticationService;
    private final MemberRepository memberRepo;
    private final TrackRepository trackRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInteraction(Member member, Track track, InteractionType type) {
        UserInteraction interaction = new UserInteraction();
        interaction.setMember(member);
        interaction.setTrack(track);
        interaction.setType(type);
        interaction.setTime(LocalDateTime.now());
        interaction.setDuration(0);

        userInteractionRepo.save(interaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addInteraction(CreateInteractionRequestDTO dto) {
        UserInteraction interaction = new UserInteraction();
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId()).orElseThrow(()-> new RuntimeException("Member not found!"));
        InteractionType type = InteractionType.valueOf(dto.interactionType());

        if(type.equals(InteractionType.PLAY)){
            Track track = trackRepo.findById(dto.trackId()).orElseThrow(()-> new RuntimeException("Track not found!"));
            interaction.setTrack(track);
            interaction.setDuration(dto.duration());
        }

        interaction.setDuration(0);
        interaction.setMember(member);
        interaction.setType(type);
        interaction.setTime(LocalDateTime.now());

        return "Added interaction successfully!";
    }
}
