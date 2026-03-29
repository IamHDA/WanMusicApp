package com.example.backend.service.implement;

import com.example.backend.Enum.AlbumStatus;
import com.example.backend.Enum.ArtistProfileStatus;
import com.example.backend.Enum.TrackStatus;
import com.example.backend.entity.Album;
import com.example.backend.entity.AlbumTrack;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.entity.Track;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImp implements AdminService {

    private final ArtistProfileRepository artistProfileRepo;
    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveArtistProfileRequest(Long artistProfileId) {
        ArtistProfile profile = artistProfileRepo.findById(artistProfileId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));
        profile.setStatus(ArtistProfileStatus.VERIFIED);
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

        return "Track approved successfully!";
    }

    @Override
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
}
