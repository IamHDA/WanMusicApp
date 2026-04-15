package com.example.backend.dto.track;

import com.example.backend.dto.ContributorDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackPreviewDTO{
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String trackUrl;
    private int duration;
    private List<ContributorDTO> contributors;
}
