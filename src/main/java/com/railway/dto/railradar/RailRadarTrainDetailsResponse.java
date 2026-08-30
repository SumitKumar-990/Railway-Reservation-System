package com.railway.dto.railradar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RailRadarTrainDetailsResponse {
    private boolean success;
    private String trainNumber;
    private String name;
    private String type;
    private String sourceCode;
    private String sourceName;
    private String destinationCode;
    private String destinationName;
    private List<String> runDays;
    private int distance;
    private int durationMinutes;
    private double avgSpeed;
    private int totalHalts;
    private String coachPosition;
    private List<RailRadarLiveStatusResponse.StationHaltInfo> halts;
}
