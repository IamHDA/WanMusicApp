package com.example.backend.service.implement;

import com.example.backend.Enum.*;
import com.example.backend.dto.CreateNotificationDTO;
import com.example.backend.dto.user.AdminArtistProfileDTO;
import com.example.backend.dto.user.AdminArtistProfilePreviewDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.ArtistProfileMapper;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.AdminService;
import com.example.backend.service.NotificationService;
import com.example.backend.service.CacheVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImp implements AdminService {

    private final ArtistProfileRepository artistProfileRepo;
    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final NotificationService notificationService;
    private final ArtistProfileMapper artistProfileMapper;
    private final CacheVersionService cacheVersionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveArtistProfileRequest(Long artistProfileId) {
        ArtistProfile profile = artistProfileRepo.findById(artistProfileId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        profile.setStatus(ArtistProfileStatus.VERIFIED);

        cacheVersionService.bumpArtistVersion();

        return "Approved artist profile request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rejectArtistProfileRequest(Long artistProfileId) {
        ArtistProfile profile = artistProfileRepo.findById(artistProfileId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        profile.setStatus(ArtistProfileStatus.REJECTED);
        return "Rejected artist profile request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveTrackRequest(Long trackId) {
        Track track = trackRepo.findById(trackId).orElseThrow(()-> new RuntimeException("Track not found!"));
        track.setStatus(TrackStatus.PUBLISHED);

        Long artistId = track.getContributions()
                .stream()
                .filter(c -> c.getRole() == ContributorRole.OWNER)
                .map(c -> c.getContributor().getId())
                .findFirst()
                .orElse(null);

        ArtistProfile artistProfile = artistProfileRepo.findById(artistId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        List<Member> followers = artistProfile.getFollowers()
                .stream()
                .map(Follower::getFollower)
                .toList();

        for(Member follower : followers){
            CreateNotificationDTO notificationDTO = new CreateNotificationDTO();
            notificationDTO.setSenderName(artistProfile.getStageName());
            notificationDTO.setTargetId(follower.getId());
            notificationDTO.setTrackId(track.getId());
            notificationDTO.setNotificationType(NotificationType.SONG_RELEASING);

            notificationService.sendNotification(notificationDTO);
        }

        cacheVersionService.bumpTrackVersion();

        return "Track approved successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rejectTrackRequest(Long trackId) {
        Track track = trackRepo.findById(trackId).orElseThrow(()-> new RuntimeException("Track not found!"));
        track.setStatus(TrackStatus.REJECTED);

        return "Track rejected successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveAlbumRequest(Long albumId) {
        Album album = albumRepo.findById(albumId).orElseThrow(()-> new RuntimeException("Album not found!"));
        List<Track> tracks = new ArrayList<>();

        album.setStatus(AlbumStatus.VERIFIED);

        for(AlbumTrack albumTrack : album.getTracks()){
            Track track = albumTrack.getTrack();
            track.setStatus(TrackStatus.REJECTED);
            tracks.add(albumTrack.getTrack());
        }
        trackRepo.saveAll(tracks);

        cacheVersionService.bumpArtistVersion();
        cacheVersionService.bumpAlbumVersion();

        return "Approved album request successfully!";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String rejectAlbumRequest(Long albumId) {
        Album album = albumRepo.findById(albumId).orElseThrow(()-> new RuntimeException("Album not found!"));
        List<Track> tracks = new ArrayList<>();
        album.setStatus(AlbumStatus.REJECTED);

        for(AlbumTrack albumTrack : album.getTracks()){
            Track track = albumTrack.getTrack();
            track.setStatus(TrackStatus.REJECTED);
            tracks.add(albumTrack.getTrack());
        }

        trackRepo.saveAll(tracks);
        albumRepo.delete(album);
        return "Rejected album request successfully!";
    }

    @Override
    public List<AdminArtistProfilePreviewDTO> getAllPendingArtistProfile() {
        return artistProfileRepo.findByStatus(ArtistProfileStatus.PENDING)
                .stream()
                .map(artistProfileMapper::toAdminPreviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminArtistProfileDTO getArtistProfileDetail(Long id) {
        ArtistProfile profile = artistProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("ArtistProfile not found"));
        return artistProfileMapper.toAdminDetailDTO(profile);
    }
}
