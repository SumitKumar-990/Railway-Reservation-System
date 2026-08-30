package com.railway.service;

import com.railway.dto.station.StationResponse;
import com.railway.model.Station;
import com.railway.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations() {
        return stationRepository.findAllByOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StationResponse> searchStations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStations();
        }
        return stationRepository.searchStations(query.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StationResponse mapToResponse(Station s) {
        return StationResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .name(s.getName())
                .city(s.getCity())
                .build();
    }
}
