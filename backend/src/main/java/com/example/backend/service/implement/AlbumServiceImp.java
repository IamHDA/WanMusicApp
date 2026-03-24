package com.example.backend.service.implement;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.PageResponse;
import com.example.backend.entity.Album;
import com.example.backend.mapper.AlbumMapper;
import com.example.backend.mapper.PageMapper;
import com.example.backend.repository.AlbumRepository;
import com.example.backend.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumServiceImp implements AlbumService {

    private final AlbumMapper albumMapper;
    private final AlbumRepository albumRepo;
    private final PageMapper pageMapper;

    @Override
    public PageResponse<AlbumPreviewDTO> getAlbumsByArtistId(Long artistId) {
        Page<Album> albums = albumRepo.findByArtistId(artistId, Pageable.ofSize(4));
        return pageMapper.toPageResponse(albums, albumMapper::toAlbumPreviewDTO);
    }
}
