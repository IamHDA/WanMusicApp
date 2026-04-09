package com.example.backend.service.implement;

import com.example.backend.dto.album.AddTrackToAlbumRequestDTO;
import com.example.backend.entity.Album;
import com.example.backend.entity.AlbumTrack;
import com.example.backend.entity.EmbeddedId.AlbumTrackId;
import com.example.backend.entity.Track;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.AlbumTrackRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.AlbumTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumTrackServiceImp implements AlbumTrackService {

    private final AlbumRepository albumRepo;
    private final TrackRepository trackRepo;
    private final AlbumTrackRepository albumTrackRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumTrackId addTrackToAlbum(AddTrackToAlbumRequestDTO dto) {
        Optional<Album> album = albumRepo.findById(dto.albumId());
        Optional<Track> track = trackRepo.findById(dto.trackId());

        if(album.isEmpty())
            throw new RuntimeException("Album not found!");
        if(track.isEmpty())
            throw new RuntimeException("Track not found!");

        AlbumTrack albumTrack = new AlbumTrack(album.get(), track.get(), dto.position());

        albumTrackRepo.save(albumTrack);

        return albumTrack.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeTrackFromAlbum(Long albumId, Long trackId) {
        Optional<AlbumTrack> albumTrack = albumTrackRepo.findByAlbum_IdAndTrack_Id(albumId, trackId);

        if(albumTrack.isEmpty())
            throw new RuntimeException("Album track not found!");

        albumTrackRepo.delete(albumTrack.get());

        return "Track removed from album successfully!";
    }
}
