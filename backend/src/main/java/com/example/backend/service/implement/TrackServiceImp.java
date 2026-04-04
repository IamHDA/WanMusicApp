package com.example.backend.service.implement;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.UpdateTrackStatusDTO;
import com.example.backend.dto.track.TrackAdminReviewDTO;
import com.example.backend.dto.track.TrackCreateDraftDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.dto.track.TrackSubmitDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.PageMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.TagRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackServiceImp implements TrackService {

    private final TrackRepository trackRepo;
    private final ArtistProfileRepository artistProfileRepo;
    private final TagRepository tagRepo;
    private final TrackMapper trackMapper;
    private final PageMapper pageMapper;

    private WebClient webClient;

    @Override
    public PageResponse<TrackAdminReviewDTO> getTracksByStatus(TrackStatus status, int index, int size) {
        return pageMapper.toPageResponse(trackRepo.findAllByStatus(status, PageRequest.of(index, size)), trackMapper::toTrackAdminReviewDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrackDraftResponseDTO createDraft(TrackCreateDraftDTO dto) {
        Track track = new Track();
        track.setTitle(dto.title());
        track.setFileKey(dto.trackKey());
        track.setThumbnailKey(dto.thumbnailKey());
        track.setDuration(dto.duration());
        track.setStatus(TrackStatus.DRAFT);
        track.setCreatedAt(LocalDateTime.now());

        trackRepo.save(track);

        List<String> predictedTags = webClient.get()
                .uri("/predict")
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .block();

        for(String tag : predictedTags){
            System.out.println(tag);
        }

        List<TrackTag> tags = tagRepo.findByNameIn(predictedTags)
                .stream()
                .map(tag -> new TrackTag(
                        track,
                        tag
                ))
                .toList();

        List<ArtistContribution> featuredArtists = artistProfileRepo.findAllById(dto.featuredArtistIds())
                .stream()
                .map(artist -> new ArtistContribution(
                        track,
                        artist
                ))
                .toList();

        track.getTags().addAll(tags);
        track.getContributions().addAll(featuredArtists);

        return trackMapper.toTrackDraftResponse(track);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTrackStatus(UpdateTrackStatusDTO dto) {
        Track track = trackRepo.findById(dto.id()).orElseThrow(()-> new RuntimeException("Track not found!"));
        track.setStatus(TrackStatus.valueOf(dto.status()));

        return "Track status updated successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitTrack(TrackSubmitDTO dto) {
        Track track = trackRepo.findById(dto.id()).orElseThrow(()-> new RuntimeException("Track not found!"));
        if(track.getStatus() != TrackStatus.DRAFT) throw new RuntimeException("Track is not in draft status!");

        track.getTags().clear();
        List<Tag> tags = tagRepo.findAllById(dto.tagIds());
        for(Tag tag : tags){
            track.getTags().add(new TrackTag(track, tag));
        }

        track.getContributions().clear();
        List<ArtistProfile> contributors = artistProfileRepo.findAllById(dto.artistIds());
        for(ArtistProfile contributor : contributors){
            track.getContributions().add(new ArtistContribution(track, contributor));
        }

        track.setStatus(TrackStatus.PENDING);

        return "Track submitted successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteTrack(Long trackId) {
        trackRepo.deleteById(trackId);
        return "Track deleted successfully!";
    }
}
