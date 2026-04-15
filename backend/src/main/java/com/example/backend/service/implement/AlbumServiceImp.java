package com.example.backend.service.implement;

import com.example.backend.Enum.AlbumStatus;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.album.CreateAlbumDraftRequestDTO;
import com.example.backend.dto.album.GetAlbumsPaginationRequest;
import com.example.backend.entity.Album;
import com.example.backend.entity.ArtistProfile;
import com.example.backend.mapper.AlbumMapper;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.repository.ArtistProfileRepository;
import com.example.backend.service.AlbumService;
import com.example.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumServiceImp implements AlbumService {

    private final AlbumMapper albumMapper;
    private final AlbumRepository albumRepo;
    private final PageMapper pageMapper;
    private final AuthenticationService authenticationService;
    private final ArtistProfileRepository artistProfileRepo;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(GetAlbumsPaginationRequest request) {
        Page<Album> albums = albumRepo.findByArtistIdAndStatus(request.artistId(), AlbumStatus.VERIFIED, PageRequest.of(request.index() - 1, request.size()));
        return pageMapper.toPageResponse(albums, albumMapper::toAlbumPreviewDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAlbumDraft(CreateAlbumDraftRequestDTO dto) {
        Album album = new Album();

        Long currentMemberId = authenticationService.getCurrentMemberId();
        ArtistProfile artistProfile = artistProfileRepo.findByMemberId(currentMemberId).orElseThrow(()-> new RuntimeException("Artist profile not found!"));

        album.setReleaseDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        album.setTitle(dto.title());
        album.setArtist(artistProfile);
        album.setStatus(AlbumStatus.DRAFT);
        album.setThumbnailKey(dto.thumbnailKey());

        albumRepo.save(album);

        return album.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitAlbum(Long albumId) {
        Optional<Album> album = albumRepo.findById(albumId);

        if(album.isEmpty())
            throw new RuntimeException("Album not found!");

        album.get().setStatus(AlbumStatus.PENDING);

        return "Submitted album successfully!";
    }

    @Override
    public PageResponse<AlbumPreviewDTO> getAllAlbums(GetAlbumsPaginationRequest request) {
        Page<Album> albums = albumRepo.findAll(PageRequest.of(request.index() - 1, request.size()));
        return pageMapper.toPageResponse(albums, albumMapper::toAlbumPreviewDTO);
    }

}
