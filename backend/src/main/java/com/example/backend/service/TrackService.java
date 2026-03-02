package com.example.backend.service;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.UpdateTrackStatusDTO;
import com.example.backend.dto.track.TrackReviewDTO;
import com.example.backend.dto.track.TrackCreateDraftDTO;
import com.example.backend.dto.track.TrackDraftResponseDTO;
import com.example.backend.dto.track.TrackSubmitDTO;

public interface TrackService {

    PageResponse<TrackReviewDTO> getTracksByStatus(TrackStatus status, int index, int size);
    TrackDraftResponseDTO createDraft(TrackCreateDraftDTO dto);
    String approveTrack(Long trackId);
    String rejectTrack(Long trackId);
    String updateTrackStatus(UpdateTrackStatusDTO dto);
    String submitTrack(TrackSubmitDTO dto);
    String deleteTrack(Long trackId);
}
