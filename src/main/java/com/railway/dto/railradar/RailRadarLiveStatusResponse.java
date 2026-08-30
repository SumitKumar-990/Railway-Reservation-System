package com.railway.dto.railradar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RailRadarLiveStatusResponse {
    private boolean success;
    private String trainNumber;
    private String trainName;
    private String status; // running, not-started, completed, on-time, delayed
    private int delayMinutes;
    private boolean isLive;
    private String lastUpdatedAt;
    private String coachPosition;
    private CurrentStationInfo currentStation;
    private NextHaltInfo nextHalt;
    private List<StationHaltInfo> halts;
    private Map<String, Object> rawData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentStationInfo {
        private String stationCode;
        private String stationName;
        private int distance;
        private String actualArrival;
        private String actualDeparture;
        private int delayArrival;
        private int delayDeparture;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextHaltInfo {
        private String stationCode;
        private String stationName;
        private String platform;
        private String scheduledArrival;
        private String expectedArrival;
        private int delayArrival;
        private int distance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StationHaltInfo {
        private int sequence;
        private String stationCode;
        private String stationName;
        private boolean isHalt;
        private String status;
        private String scheduledArrival;
        private String scheduledDeparture;
        private String actualArrival;
        private String actualDeparture;
        private int delayArrival;
        private String platform;
        private int distance;
    }
}
