package com.example.backend.controller;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackCreateDraftDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackSubmitDTO;
import com.example.backend.service.S3StorageService;
import com.example.backend.service.TrackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/track")
public class TrackController {

    private final TrackService trackService;
    private final S3StorageService s3StorageService;

    @GetMapping("/find")
    public ResponseEntity<String> getTrack(@RequestParam(name = "trackFile") String fileName){
        return ResponseEntity.ok(s3StorageService.getGetPresignedUrl(fileName, "songs"));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<TrackPreviewDTO>> searchTrackAddToPlaylist(
            @RequestParam(name = "existedTrackIds") List<Long> existedTrackIds,
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "pageNumber") int pageNumber,
            @RequestParam(name = "pageSize") int pageSize)
    {
        return ResponseEntity.ok(trackService.searchTracksAddToPlaylist(existedTrackIds, keyword, pageNumber, pageSize));
    }

    @PostMapping("/submitDraft")
    public ResponseEntity<TrackDraftResponseDTO> submitDraft(@RequestBody TrackCreateDraftDTO dto) throws IOException {
        return ResponseEntity.ok(trackService.createDraft(dto));
    }

    @PostMapping("/submitFinalize")
    public ResponseEntity<String> submitFinalize(@RequestBody TrackSubmitDTO dto){
        return ResponseEntity.ok(trackService.submitTrack(dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTrack(@PathVariable Long id){
        return ResponseEntity.ok(trackService.deleteTrack(id));
    }
}
