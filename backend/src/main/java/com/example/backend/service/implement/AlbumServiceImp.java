package com.example.backend.service.implement;

import com.example.backend.Enum.AlbumStatus;
import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.album.CreateAlbumRequestDTO;
import com.example.backend.entity.Album;
import com.example.backend.entity.AlbumTrack;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.entity.Track;
import com.example.backend.mapper.AlbumMapper;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.AlbumTrackRepository;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.repository.TrackRepository;
import com.example.backend.service.AlbumService;
import com.example.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImp implements AlbumService {

    private final AlbumMapper albumMapper;
    private final AlbumRepository albumRepo;
    private final PageMapper pageMapper;
    private final TrackRepository trackRepo;
    private final AlbumTrackRepository albumTrackRepo;
    private final AuthenticationService authenticationService;
    private final ArtistProfileRepository artistProfileRepo;

    @Override
    public PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(Long artistId) {
        Page<Album> albums = albumRepo.findByArtistId(artistId, Pageable.ofSize(4));
        return pageMapper.toPageResponse(albums, albumMapper::toAlbumPreviewDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAlbumRequest(CreateAlbumRequestDTO dto) {
        Album album = new Album();

        Long currentMemberId = authenticationService.getCurrentMemberId();
        ArtistProfile artistProfile = artistProfileRepo.findByMemberId(currentMemberId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        album.setTitle(dto.title());
        album.setArtist(artistProfile);
        album.setStatus(AlbumStatus.PENDING);
        album.setThumbnailKey(dto.thumbnailKey());

        albumRepo.save(album);

        List<AlbumTrack> albumTracks = new ArrayList<>();

        dto.tracksIdAndPosition().forEach((k, v) -> albumTracks.add(
                new AlbumTrack(album, trackRepo.findById(k).orElseThrow(()-> new RuntimeException("Track not found!")), v)));

        albumTrackRepo.saveAll(albumTracks);

        return "Created album request successfully!";
    }


}
