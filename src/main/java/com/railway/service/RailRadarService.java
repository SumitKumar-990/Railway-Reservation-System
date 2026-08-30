package com.railway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.dto.railradar.RailRadarLiveStatusResponse;
import com.railway.dto.railradar.RailRadarTrainDetailsResponse;
import com.railway.model.*;
import com.railway.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class RailRadarService {

    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TrackingService trackingService;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final SeatClassRepository seatClassRepository;

    public RailRadarService(
            @Value("${app.railradar.api-key:}") String apiKey,
            @Value("${app.railradar.base-url:https://api.railradar.in/v1}") String baseUrl,
            TrackingService trackingService,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            SeatClassRepository seatClassRepository) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.objectMapper = new ObjectMapper();
        this.trackingService = trackingService;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.seatClassRepository = seatClassRepository;
    }

    public RailRadarLiveStatusResponse getLiveStatus(String trainNumber) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return buildFallbackLiveStatus(trainNumber);
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/trains/" + trainNumber + "/live"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");

                List<RailRadarLiveStatusResponse.StationHaltInfo> halts = new ArrayList<>();
                JsonNode routeNode = data.path("route");
                if (routeNode.isArray()) {
                    for (JsonNode haltNode : routeNode) {
                        if (haltNode.path("isHalt").asBoolean(true)) {
                            halts.add(RailRadarLiveStatusResponse.StationHaltInfo.builder()
                                    .sequence(haltNode.path("sequence").asInt())
                                    .stationCode(haltNode.path("stationCode").asText())
                                    .stationName(haltNode.path("stationName").asText())
                                    .isHalt(haltNode.path("isHalt").asBoolean())
                                    .status(haltNode.path("status").asText())
                                    .scheduledArrival(haltNode.path("scheduledArrival").asText(null))
                                    .scheduledDeparture(haltNode.path("scheduledDeparture").asText(null))
                                    .actualArrival(haltNode.path("actualArrival").asText(null))
                                    .actualDeparture(haltNode.path("actualDeparture").asText(null))
                                    .delayArrival(haltNode.path("delayArrival").asInt(0))
                                    .platform(haltNode.path("platform").asText(""))
                                    .distance(haltNode.path("distance").asInt(0))
                                    .build());
                        }
                    }
                }

                RailRadarLiveStatusResponse.CurrentStationInfo currentStation = null;
                JsonNode curStNode = data.path("currentStation");
                if (!curStNode.isNull() && curStNode.isObject()) {
                    currentStation = RailRadarLiveStatusResponse.CurrentStationInfo.builder()
                            .stationCode(curStNode.path("stationCode").asText())
                            .stationName(curStNode.path("stationName").asText())
                            .distance(curStNode.path("distance").asInt(0))
                            .actualArrival(curStNode.path("actualArrival").asText(null))
                            .actualDeparture(curStNode.path("actualDeparture").asText(null))
                            .delayArrival(curStNode.path("delayArrival").asInt(0))
                            .delayDeparture(curStNode.path("delayDeparture").asInt(0))
                            .build();
                }

                RailRadarLiveStatusResponse.NextHaltInfo nextHalt = null;
                JsonNode nextHaltNode = data.path("nextHalt");
                if (!nextHaltNode.isNull() && nextHaltNode.isObject()) {
                    nextHalt = RailRadarLiveStatusResponse.NextHaltInfo.builder()
                            .stationCode(nextHaltNode.path("stationCode").asText())
                            .stationName(nextHaltNode.path("stationName").asText())
                            .platform(nextHaltNode.path("platform").asText(""))
                            .scheduledArrival(nextHaltNode.path("scheduledArrival").asText(null))
                            .expectedArrival(nextHaltNode.path("expectedArrival").asText(null))
                            .delayArrival(nextHaltNode.path("delayArrival").asInt(0))
                            .distance(nextHaltNode.path("distance").asInt(0))
                            .build();
                }

                RailRadarLiveStatusResponse liveStatus = RailRadarLiveStatusResponse.builder()
                        .success(true)
                        .trainNumber(data.path("trainNumber").asText(trainNumber))
                        .trainName(data.path("trainName").asText())
                        .status(data.path("status").asText("running"))
                        .delayMinutes(data.path("delayMinutes").asInt(0))
                        .isLive(data.path("isLive").asBoolean(true))
                        .lastUpdatedAt(data.path("lastUpdatedAt").asText(LocalDateTime.now().toString()))
                        .coachPosition(data.path("coachPosition").asText(null))
                        .currentStation(currentStation)
                        .nextHalt(nextHalt)
                        .halts(halts)
                        .build();

                // Broadcast live update over WebSocket
                Map<String, Object> wsPayload = new HashMap<>();
                wsPayload.put("trainNumber", trainNumber);
                wsPayload.put("status", liveStatus.getStatus());
                wsPayload.put("delayMinutes", liveStatus.getDelayMinutes());
                wsPayload.put("currentStation", currentStation);
                wsPayload.put("nextHalt", nextHalt);
                wsPayload.put("lastUpdatedAt", liveStatus.getLastUpdatedAt());
                trackingService.broadcastUpdate(0L, wsPayload);

                return liveStatus;
            } else {
                log.warn("RailRadar API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to query RailRadar API for train {}", trainNumber, e);
        }

        // Fallback for custom demo trains or when network is offline
        return buildFallbackLiveStatus(trainNumber);
    }

    public RailRadarTrainDetailsResponse getTrainDetails(String trainNumber) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/trains/" + trainNumber))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                JsonNode train = data.path("train");

                List<String> runDays = new ArrayList<>();
                JsonNode runDaysNode = train.path("runDays");
                if (runDaysNode.isArray()) {
                    for (JsonNode d : runDaysNode) {
                        runDays.add(d.asText().toUpperCase());
                    }
                }

                List<RailRadarLiveStatusResponse.StationHaltInfo> halts = new ArrayList<>();
                JsonNode routeNode = data.path("route");
                if (routeNode.isArray()) {
                    for (JsonNode h : routeNode) {
                        if (h.path("isHalt").asBoolean(true)) {
                            halts.add(RailRadarLiveStatusResponse.StationHaltInfo.builder()
                                    .sequence(h.path("sequence").asInt())
                                    .stationCode(h.path("stationCode").asText())
                                    .stationName(h.path("stationName").asText())
                                    .isHalt(h.path("isHalt").asBoolean())
                                    .scheduledArrival(h.path("scheduledArrival").asText(null))
                                    .scheduledDeparture(h.path("scheduledDeparture").asText(null))
                                    .platform(h.path("platform").asText(""))
                                    .distance(h.path("distance").asInt(0))
                                    .build());
                        }
                    }
                }

                return RailRadarTrainDetailsResponse.builder()
                        .success(true)
                        .trainNumber(train.path("number").asText(trainNumber))
                        .name(train.path("name").asText())
                        .type(train.path("type").asText("Express"))
                        .sourceCode(train.path("source").path("code").asText())
                        .sourceName(train.path("source").path("name").asText())
                        .destinationCode(train.path("destination").path("code").asText())
                        .destinationName(train.path("destination").path("name").asText())
                        .runDays(runDays)
                        .distance(train.path("distance").asInt(0))
                        .durationMinutes(train.path("duration").asInt(0))
                        .avgSpeed(train.path("avgSpeed").asDouble(0.0))
                        .totalHalts(train.path("totalHalts").asInt(halts.size()))
                        .coachPosition(train.path("coachPosition").asText(null))
                        .halts(halts)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to query RailRadar train details for {}", trainNumber, e);
        }

        return buildFallbackTrainDetails(trainNumber);
    }

    @Transactional
    public void importTrainsForStation(String stationCode, String destinationCode) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/stations/" + stationCode + "/trains"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode trainsArray = root.path("data").path("trains");
            if (!trainsArray.isArray()) {
                return;
            }

            int imported = 0;
            for (JsonNode item : trainsArray) {
                if (imported >= 8) break; // Limit to 8 trains per sync to keep UI fast
                JsonNode trainNode = item.path("train");
                String trainNum = trainNode.path("number").asText();
                String trainName = trainNode.path("name").asText();
                String dest = trainNode.path("destination").path("code").asText();

                // If destination matches or if looking for general trains
                boolean isDestMatch = destinationCode == null || dest.equalsIgnoreCase(destinationCode) ||
                        destinationCode.equalsIgnoreCase(trainNode.path("destination").path("name").asText());

                if (isDestMatch || trainRepository.findByTrainNumber(trainNum).isEmpty()) {
                    importSingleTrain(trainNum, trainName, trainNode);
                    imported++;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to import real trains from RailRadar for station {}", stationCode, e);
        }
    }

    @Transactional
    public void importSingleTrain(String trainNumber, String trainName, JsonNode trainMeta) {
        try {
            if (trainRepository.findByTrainNumber(trainNumber).isPresent()) {
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/trains/" + trainNumber))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            JsonNode route = data.path("route");
            if (!route.isArray() || route.isEmpty()) return;

            Train train = new Train();
            train.setTrainNumber(trainNumber);
            train.setName(trainName != null && !trainName.isEmpty() ? trainName : data.path("train").path("name").asText("Express " + trainNumber));
            
            List<String> runDaysList = new ArrayList<>();
            JsonNode runDaysNode = data.path("train").path("runDays");
            if (runDaysNode.isArray()) {
                for (JsonNode rd : runDaysNode) {
                    runDaysList.add(rd.asText().toUpperCase());
                }
            }
            train.setRunningDays(runDaysList.isEmpty() ? "DAILY" : String.join(",", runDaysList));

            Set<TrainStop> stops = new HashSet<>();
            int seq = 1;
            for (JsonNode h : route) {
                if (h.path("isHalt").asBoolean(true)) {
                    String stCode = h.path("stationCode").asText();
                    String stName = h.path("stationName").asText();
                    Station station = getOrCreateStation(stCode, stName);

                    TrainStop stop = new TrainStop();
                    stop.setTrain(train);
                    stop.setStation(station);
                    stop.setSequenceNumber(seq++);
                    stop.setDistanceFromOriginKm(h.path("distance").asInt(seq * 50));
                    stop.setDayOffset(h.path("arrivalDay").asInt(0));

                    String arr = h.path("scheduledArrival").asText(null);
                    String dep = h.path("scheduledDeparture").asText(null);
                    stop.setArrivalTime(parseLocalTime(arr));
                    stop.setDepartureTime(parseLocalTime(dep));

                    stops.add(stop);
                }
            }
            train.setStops(stops);

            // Add default class configs
            Set<TrainClassConfig> configs = new HashSet<>();
            SeatClass sl = getOrCreateSeatClass("SL", "Sleeper");
            SeatClass ac3 = getOrCreateSeatClass("3A", "AC 3 Tier");
            SeatClass ac2 = getOrCreateSeatClass("2A", "AC 2 Tier");
            SeatClass ac1 = getOrCreateSeatClass("1A", "AC First Class");

            configs.add(createConfig(train, sl, 200, 20, 0.45, 120));
            configs.add(createConfig(train, ac3, 150, 15, 0.80, 250));
            configs.add(createConfig(train, ac2, 80, 8, 1.20, 400));
            configs.add(createConfig(train, ac1, 30, 3, 2.00, 600));
            train.setClassConfigs(configs);

            trainRepository.save(train);
            log.info("Successfully imported real Indian Railways train {} ({}) from RailRadar with {} halts", trainNumber, train.getName(), stops.size());
        } catch (Exception e) {
            log.warn("Failed to import single train {} from RailRadar", trainNumber, e);
        }
    }

    private LocalTime parseLocalTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return LocalTime.of(12, 0);
        try {
            if (timeStr.contains("T")) {
                return LocalDateTime.parse(timeStr).toLocalTime();
            } else if (timeStr.contains(":")) {
                String[] parts = timeStr.split(":");
                return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        } catch (Exception ignored) {}
        return LocalTime.of(12, 0);
    }

    private Station getOrCreateStation(String code, String name) {
        return stationRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            Station st = new Station();
            st.setCode(code.toUpperCase());
            st.setName(name != null ? name : code);
            st.setCity(name != null ? name : code);
            return stationRepository.save(st);
        });
    }

    private SeatClass getOrCreateSeatClass(String code, String label) {
        return seatClassRepository.findByCode(code).orElseGet(() -> {
            SeatClass sc = new SeatClass();
            sc.setCode(code);
            sc.setLabel(label);
            return seatClassRepository.save(sc);
        });
    }

    private TrainClassConfig createConfig(Train t, SeatClass sc, int seats, int rac, double farePerKm, double base) {
        TrainClassConfig c = new TrainClassConfig();
        c.setTrain(t);
        c.setSeatClass(sc);
        c.setTotalSeats(seats);
        c.setRacQuota(rac);
        c.setFarePerKm(farePerKm);
        c.setBaseFare(base);
        return c;
    }

    private RailRadarLiveStatusResponse buildFallbackLiveStatus(String trainNumber) {
        Optional<Train> localTrain = trainRepository.findByTrainNumber(trainNumber);
        String name = localTrain.map(Train::getName).orElse("Express Train " + trainNumber);

        return RailRadarLiveStatusResponse.builder()
                .success(true)
                .trainNumber(trainNumber)
                .trainName(name)
                .status("on-time")
                .delayMinutes(0)
                .isLive(true)
                .lastUpdatedAt(LocalDateTime.now().toString())
                .coachPosition("ENG-SL1-SL2-3A1-3A2-2A1-1A1-EOG")
                .nextHalt(RailRadarLiveStatusResponse.NextHaltInfo.builder()
                        .stationCode("NDLS")
                        .stationName("New Delhi")
                        .platform("1")
                        .delayArrival(0)
                        .build())
                .halts(Collections.emptyList())
                .build();
    }

    private RailRadarTrainDetailsResponse buildFallbackTrainDetails(String trainNumber) {
        Optional<Train> localTrain = trainRepository.findByTrainNumber(trainNumber);
        String name = localTrain.map(Train::getName).orElse("Express " + trainNumber);
        String runningDays = localTrain.map(Train::getRunningDays).orElse("DAILY");

        return RailRadarTrainDetailsResponse.builder()
                .success(true)
                .trainNumber(trainNumber)
                .name(name)
                .type("Express")
                .sourceCode("HWH")
                .sourceName("Howrah")
                .destinationCode("NDLS")
                .destinationName("New Delhi")
                .runDays(Arrays.asList(runningDays.split(",")))
                .distance(1400)
                .durationMinutes(1020)
                .avgSpeed(82.0)
                .totalHalts(6)
                .coachPosition("ENG-SL1-SL2-3A1-3A2-2A1-1A1-EOG")
                .halts(Collections.emptyList())
                .build();
    }
}
