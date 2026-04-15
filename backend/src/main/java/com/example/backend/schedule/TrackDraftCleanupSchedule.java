package com.example.backend.schedule;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.entity.Track;
import com.example.backend.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackDraftCleanupSchedule {

    private final TrackRepository trackRepo;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupLongTrackDrafts (){
        log.info("Cleaning up long track drafts...");

        LocalDate today = LocalDate.now();
        Page<Track> tracks = trackRepo.findAllByStatusAndNotInAlbum(TrackStatus.DRAFT, Pageable.unpaged());

        if (tracks.getTotalElements() == 0) {
            log.info("No long track drafts to clean up.");
        }

        for(Track draft : tracks.getContent()){
            LocalDate draftCreateTime = LocalDate.from(draft.getCreatedAt());

            if(draftCreateTime.isEqual(today.minusDays(2))){
                trackRepo.delete(draft);
                log.info("Deleted long track draft: " + draft.getTitle());
            }
        }

        log.info("Long track draft cleanup completed.");
    }

}
