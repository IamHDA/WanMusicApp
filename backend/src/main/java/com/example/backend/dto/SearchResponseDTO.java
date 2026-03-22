package com.example.backend.dto;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResponseDTO {

    private PageResponse<TrackPreviewDTO> trackPreviewDTOS;
    private PageResponse<AlbumPreviewDTO> albumPreviewDTOS;
    private PageResponse<UserPreviewDTO> memberPreviewDTOS;
    private PageResponse<UserPreviewDTO> artistPreviewDTOS;

}
