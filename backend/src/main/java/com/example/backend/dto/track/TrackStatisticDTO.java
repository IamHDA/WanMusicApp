package com.example.backend.dto.track;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrackStatisticDTO {

    private Long playCount;
    private Long listenDuration;
    private Long favoriteCount;
    private Long shareCount;
    private Long jamCount;

}
