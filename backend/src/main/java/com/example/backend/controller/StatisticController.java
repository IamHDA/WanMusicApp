package com.example.backend.controller;

import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.track.TrackStatisticDTO;
import com.example.backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistic")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/getStatistic/{id}")
    public ResponseEntity<TrackStatisticDTO> getTrackStatistic(@PathVariable Long id){
        return ResponseEntity.ok(statisticService.getTrackStatistic(id));
    }

    @GetMapping("/getTopTracks")
    public ResponseEntity<List<TrackPreviewDTO>> getTopTracks(){
        return ResponseEntity.ok(statisticService.getTopTracks());
    }

}
