package com.example.backend.service.implement;

import com.example.backend.Enum.InteractionType;
import com.example.backend.dto.CreateInteractionRequestDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.repository.UserInteractionRepository;
import com.example.backend.repository.UserTagPreferenceRepository;
import com.example.backend.service.UserInteractionService;
import com.example.backend.entity.EmbeddedId.UserTagPreferenceId;
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
    private final UserTagPreferenceRepository userTagPrefRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addInteraction(CreateInteractionRequestDTO dto) {
        Member member = memberRepo.findById(authenticationService.getCurrentMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found!"));

        Track track = trackRepo.findById(dto.trackId())
                .orElseThrow(() -> new RuntimeException("Track not found!"));

        InteractionType type = InteractionType.valueOf(dto.interactionType());

        UserInteraction interaction = new UserInteraction();
        interaction.setMember(member);
        interaction.setTrack(track);
        interaction.setType(type);
        interaction.setTime(LocalDateTime.now());
        // Gán đúng thời gian nghe thực tế từ FE gửi lên
        interaction.setDuration(dto.duration());

        userInteractionRepo.save(interaction);

        // LOGIC THỐNG KÊ GỢI Ý (Chỉ chạy khi loại tương tác là PLAY)
        if (type.equals(InteractionType.PLAY)) {
            // Ngưỡng đề xuất: Nghe >= 30s VÀ >= 50% bài hát
            if (dto.duration() >= 30 && dto.duration() >= (track.getDuration() / 2)) {
                for (TrackTag trackTag : track.getTags()) {
                    Tag tag = trackTag.getTag();
                    UserTagPreference pref = userTagPrefRepo.findById(new UserTagPreferenceId(member.getId(), tag.getId()))
                            .orElse(new UserTagPreference(member, tag, 0));

                    pref.setScore(pref.getScore() + 1); // Tích lũy gu âm nhạc
                    userTagPrefRepo.save(pref);
                }
            }
        }
        return "Interaction recorded";
    }
}
