package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.album.CreateAlbumDraftRequestDTO;
import com.example.backend.dto.album.GetAlbumsPaginationRequest;
import com.example.backend.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    public record AlbumId(Long id){};

    @GetMapping("/albums/{artistId}")
    public ResponseEntity<PageResponse<AlbumPreviewDTO>> getArtistAlbums(
            @RequestParam(name = "artistId") Long artistId,
            @RequestParam(name = "pageNumber") int index,
            @RequestParam(name = "pageSize") int size
    ){
        GetAlbumsPaginationRequest request = new GetAlbumsPaginationRequest(artistId, index, size);
        return ResponseEntity.ok(albumService.getAlbumsByArtistId(request));
    }

    @PostMapping("/draft")
    public ResponseEntity<String> saveAlbumDraft(@RequestBody CreateAlbumDraftRequestDTO dto){
        return ResponseEntity.ok(albumService.createAlbumDraft(dto));
    }

    @PutMapping("/submit")
    public ResponseEntity<String> submitAlbumDraft(@RequestBody AlbumId id){
        return ResponseEntity.ok(albumService.submitAlbum(id.id));
    }

}
