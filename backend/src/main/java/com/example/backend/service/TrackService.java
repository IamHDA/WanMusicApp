package com.example.backend.service;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.playlist.SearchTrackRequestDTO;
import com.example.backend.dto.track.*;
import com.example.backend.dto.user.ArtistDashboardDTO;

import java.io.IOException;
import java.util.List;

public interface TrackService {

    TrackPreviewDTO getTrack(Long trackId);
    PageResponse<TrackAdminReviewDTO> getTracksByStatus(TrackStatus status, int index, int size);
    TrackDraftResponseDTO createDraft(TrackCreateDraftDTO dto) throws IOException;
    PageResponse<TrackPreviewDTO> searchTracksAddToPlaylist(List<Long> existedTrackIds, String keyword, int index, int size);
    String updateTrackStatus(UpdateTrackStatusDTO dto);
    String submitTrack(TrackSubmitDTO dto);
    String deleteTrack(Long trackId);
    ArtistDashboardDTO getArtistDashboard();
}
