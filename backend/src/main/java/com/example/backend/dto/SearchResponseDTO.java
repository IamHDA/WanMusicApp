package com.example.backend.dto;

import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.ArtistProfilePreviewDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResponseDTO {

    private PageResponse<TrackPreviewDTO> trackPreviewDTOS;
    private PageResponse<AlbumPreviewDTO> albumPreviewDTOS;
    private PageResponse<MemberProfilePreviewDTO> memberPreviewDTOS;
    private PageResponse<ArtistProfilePreviewDTO> artistPreviewDTOS;

}
