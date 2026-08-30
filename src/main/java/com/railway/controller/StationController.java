package com.railway.controller;

import com.railway.dto.station.StationResponse;
import com.railway.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StationResponse>> searchStations(@RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(stationService.searchStations(q));
    }
}
