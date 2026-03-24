package com.example.backend.service.implement;

import com.example.backend.Enum.InterationType;
import com.example.backend.entity.Member;
import com.example.backend.entity.Track;
import com.example.backend.entity.UserInteraction;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInteraction(Member member, Track track, InterationType type) {
        UserInteraction interaction = new UserInteraction();
        interaction.setMember(member);
        interaction.setTrack(track);
        interaction.setType(type);
        interaction.setTime(LocalDateTime.now());
        interaction.setDuration(0);

        userInteractionRepo.save(interaction);
    }
}
