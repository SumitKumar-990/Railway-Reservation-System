package com.railway.controller;

import com.railway.dto.railradar.RailRadarLiveStatusResponse;
import com.railway.dto.railradar.RailRadarTrainDetailsResponse;
import com.railway.dto.train.TrainSearchResult;
import com.railway.service.RailRadarService;
import com.railway.service.TrainSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainSearchService trainSearchService;
    private final RailRadarService railRadarService;

    @GetMapping("/search")
    public ResponseEntity<List<TrainSearchResult>> searchTrains(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(trainSearchService.searchTrains(from, to, date));
    }

    @GetMapping("/{trainNumber}/live")
    public ResponseEntity<RailRadarLiveStatusResponse> getLiveStatus(@PathVariable String trainNumber) {
        return ResponseEntity.ok(railRadarService.getLiveStatus(trainNumber));
    }

    @GetMapping("/{trainNumber}/details")
    public ResponseEntity<RailRadarTrainDetailsResponse> getTrainDetails(@PathVariable String trainNumber) {
        return ResponseEntity.ok(railRadarService.getTrainDetails(trainNumber));
    }
}
