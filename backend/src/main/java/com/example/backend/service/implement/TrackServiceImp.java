package com.example.backend.service.implement;

import com.example.backend.Enum.ContributorRole;
import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.ContributorDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.*;
import com.example.backend.dto.user.ArtistDashboardDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.PageMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.TagRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.S3StorageService;
import com.example.backend.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackServiceImp implements TrackService {

    private final TrackRepository trackRepo;
    private final ArtistProfileRepository artistProfileRepo;
    private final TagRepository tagRepo;
    private final TrackMapper trackMapper;
    private final PageMapper pageMapper;
    private final S3StorageService s3StorageService;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional(readOnly = true)
    public TrackPreviewDTO getTrack(Long trackId) {
        Track track = trackRepo.findById(trackId).orElseThrow(()-> new RuntimeException("Track not found!"));
        return trackMapper.toTrackPreviewDTO(track);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackAdminReviewDTO> getTracksByStatus(TrackStatus status, int index, int size) {
        return pageMapper.toPageResponse(trackRepo.findAllByStatusAndNotInAlbum(status, PageRequest.of(index, size)), trackMapper::toTrackAdminReviewDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrackDraftResponseDTO createDraft(TrackCreateDraftDTO dto) throws IOException {
        Track track = new Track();
        track.setTitle(dto.title());
        track.setFileKey(dto.trackKey());
        track.setThumbnailKey(dto.thumbnailKey());
        track.setDuration(dto.duration());
        track.setStatus(TrackStatus.DRAFT);
        track.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        trackRepo.save(track);

        List<String> predictedTags = new ArrayList<>();
        try {
            HttpClient jdkHttpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            WebClient webClient = WebClient.builder()
                    .clientConnector(new JdkClientHttpConnector(jdkHttpClient))
                    .baseUrl("http://localhost:1111")
                    .build();

            InputStream stream = s3StorageService.getFile(track.getFileKey(), "songs");
            byte[] fileBytes = stream.readAllBytes();

            String fileNameWithExt = track.getFileKey();
            if (!fileNameWithExt.contains(".")) {
                fileNameWithExt += ".mp3";
            }
            final String finalFileName = fileNameWithExt;

            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return finalFileName;
                }
            };
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", resource)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\"" + finalFileName + "\"")
                    .contentType(MediaType.parseMediaType("audio/mpeg"));

            // Gọi AI Service
            predictedTags = webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block();

            if (predictedTags != null) {
                System.out.println("AI Predicted Tags: " + String.join(", ", predictedTags));
            } else {
                predictedTags = new ArrayList<>(); // Đảm bảo không bị null
            }

        } catch (Exception e) {
            // Log lại lỗi nhưng KHÔNG NÉM RA (throw) để luồng tiếp tục chạy
            System.err.println("[CẢNH BÁO] Không thể kết nối đến AI Service (http://localhost:1111) hoặc quá trình dự đoán thất bại.");
            System.err.println("Lý do: " + e.getMessage());
            System.err.println("-> Bỏ qua gán nhãn tự động, tiếp tục lưu nháp Track.");
            predictedTags = new ArrayList<>(); // Gán rỗng để chạy tiếp
        }

        List<TrackTag> tags = tagRepo.findByNameIn(predictedTags)
                .stream()
                .map(tag -> new TrackTag(
                        track,
                        tag
                ))
                .collect(Collectors.toList());

        List<ArtistContribution> contributions = new ArrayList<>();

        if (dto.featuredArtistDTO() != null) {
            for(ContributorDTO contributorDTO : dto.featuredArtistDTO()){
                ArtistProfile contributor = artistProfileRepo.findById(contributorDTO.getId())
                        .orElseThrow(()-> new RuntimeException("Artist not found for draft ID: " + contributorDTO.getId()));
                ContributorRole role = ContributorRole.valueOf(contributorDTO.getRole().toUpperCase());
                contributions.add(new ArtistContribution(track, contributor, role));
            }
        }
        track.setTags(tags);
        track.setContributions(contributions);
        trackRepo.save(track);

        return trackMapper.toTrackDraftResponse(track);
    }

    @Override
    public PageResponse<TrackPreviewDTO> searchTracksAddToPlaylist(List<Long> existedTrackIds, String keyword, int index, int size) {
        Page<Track> foundedTracks = trackRepo.search(existedTrackIds, keyword, PageRequest.of(index - 1, size));
        return pageMapper.toPageResponse(foundedTracks, trackMapper::toTrackPreviewDTO);
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
        track.getContributions().clear();

        trackRepo.flush();

        List<Tag> tags = tagRepo.findAllById(dto.tagIds());
        for(Tag tag : tags){
            track.getTags().add(new TrackTag(track, tag));
        }

        // Ép Owner luôn có
        Long currentUserId = authenticationService.getCurrentMemberId();
        ArtistProfile ownerProfile = artistProfileRepo.findByMemberId(currentUserId)
                .orElseThrow(()-> new RuntimeException("Chỉ tài khoản Artist hợp lệ mới được submit track!"));
        track.getContributions().add(new ArtistContribution(track, ownerProfile, ContributorRole.OWNER));

        for(ContributorDTO contributorDTO : dto.contributors()){
            ArtistProfile contributor = artistProfileRepo.findById(contributorDTO.getId())
                    .orElseThrow(()-> new RuntimeException("Artist ID không tồn tại trên hệ thống: " + contributorDTO.getId() + ". Vui lòng kiểm tra lại Dữ liệu Frontend truyền lên !"));
            ContributorRole role = ContributorRole.valueOf(contributorDTO.getRole().toUpperCase());
            track.getContributions().add(new ArtistContribution(track, contributor, role));
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

    @Override
    public ArtistDashboardDTO getArtistDashboard() {
        Long currentUserId = authenticationService.getCurrentMemberId();
        ArtistProfile ownerProfile = artistProfileRepo.findByMemberId(currentUserId)
                .orElseThrow(()-> new RuntimeException("Bạn không phải Artist!"));
        
        List<Track> tracks = trackRepo.findTracksByOwnerId(ownerProfile.getId());
        List<TrackArtistDTO> trackDTOs = tracks.stream().map(t -> new TrackArtistDTO(
                t.getId(),
                t.getTitle(),
                t.getThumbnailKey() != null ? s3StorageService.getGetPresignedUrl(t.getThumbnailKey(), "thumbnails") : null,
                t.getDuration(),
                t.getStatus().name()
        )).toList();
        
        long totalFans = ownerProfile.getFollowers() != null ? ownerProfile.getFollowers().size() : 0;
        long totalDrops = tracks.size();
        
        return new ArtistDashboardDTO(totalFans, totalDrops, trackDTOs);
    }
}
