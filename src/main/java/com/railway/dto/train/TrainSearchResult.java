package com.railway.dto.train;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TrainSearchResult {
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String fromCode;
    private String toStation;
    private String toCode;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private int dayOffset;
    private int distanceKm;
    private List<ClassAvailability> classAvailabilities;
}
