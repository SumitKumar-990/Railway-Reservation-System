package com.railway.service;

import com.railway.dto.train.ClassAvailability;
import com.railway.dto.train.TrainSearchResult;
import com.railway.model.*;
import com.railway.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainSearchService {

    private final TrainRepository trainRepository;
    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final StationRepository stationRepository;
    private final TrainClassConfigRepository trainClassConfigRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final FareCalculator fareCalculator;
    private final RailRadarService railRadarService;

    @Transactional
    public List<TrainSearchResult> searchTrains(String fromQuery, String toQuery, LocalDate date) {
        if (fromQuery == null || toQuery == null || date == null) {
            return Collections.emptyList();
        }

        Optional<Station> fromStationOpt = resolveStation(fromQuery);
        Optional<Station> toStationOpt = resolveStation(toQuery);

        if (fromStationOpt.isEmpty() || toStationOpt.isEmpty()) {
            log.warn("Could not resolve stations for fromQuery='{}', toQuery='{}'", fromQuery, toQuery);
            return Collections.emptyList();
        }

        Station fromStation = fromStationOpt.get();
        Station toStation = toStationOpt.get();

        List<Train> trainsBetween = trainRepository.findTrainsBetweenStations(fromStation.getCode(), toStation.getCode());

        // If no local trains found or very few, sync live real trains from RailRadar
        if (trainsBetween.isEmpty()) {
            try {
                railRadarService.importTrainsForStation(fromStation.getCode(), toStation.getCode());
                trainsBetween = trainRepository.findTrainsBetweenStations(fromStation.getCode(), toStation.getCode());
            } catch (Exception e) {
                log.warn("Error dynamically importing trains from RailRadar: {}", e.getMessage());
            }
        }

        List<TrainSearchResult> results = new ArrayList<>();

        for (Train train : trainsBetween) {
            if (train.runsOn(date.getDayOfWeek())) {
                TrainRun trainRun = getOrCreateTrainRun(train, date);
                
                Optional<TrainStop> fromStopOpt = trainStopRepository.findByTrainAndStation(train, fromStation);
                Optional<TrainStop> toStopOpt = trainStopRepository.findByTrainAndStation(train, toStation);

                if (fromStopOpt.isEmpty() || toStopOpt.isEmpty()) {
                    continue;
                }

                TrainStop fromStop = fromStopOpt.get();
                TrainStop toStop = toStopOpt.get();
                        
                int distanceKm = Math.abs(toStop.getDistanceFromOriginKm() - fromStop.getDistanceFromOriginKm());
                if (distanceKm == 0) distanceKm = 450; // Fallback distance if same index
                
                List<TrainClassConfig> classConfigs = trainClassConfigRepository.findByTrain(train);
                List<ClassAvailability> availabilities = new ArrayList<>();
                
                for (TrainClassConfig config : classConfigs) {
                    Optional<SeatInventory> inventoryOpt = seatInventoryRepository.findByTrainRunAndSeatClass(trainRun, config.getSeatClass());
                    if (inventoryOpt.isEmpty()) {
                        continue;
                    }
                    SeatInventory inventory = inventoryOpt.get();
                            
                    int availableSeats = inventory.availableConfirmed();
                    int racAvailable = inventory.availableRac();
                    int waitlistCount = inventory.getWaitlistCount();
                    double fare = fareCalculator.calculateFare(config, fromStop, toStop, 1);
                    
                    String statusLabel;
                    if (availableSeats > 0) {
                        statusLabel = "AVAILABLE " + availableSeats;
                    } else if (racAvailable > 0) {
                        statusLabel = "RAC " + racAvailable;
                    } else {
                        statusLabel = "WL " + (waitlistCount + 1);
                    }
                    
                    ClassAvailability availability = new ClassAvailability(
                            config.getSeatClass().getCode(),
                            config.getSeatClass().getLabel(),
                            availableSeats,
                            racAvailable,
                            waitlistCount,
                            fare,
                            statusLabel
                    );
                    availabilities.add(availability);
                }
                
                TrainSearchResult result = new TrainSearchResult(
                        train.getTrainNumber(),
                        train.getName(),
                        fromStation.getName(),
                        fromStation.getCode(),
                        toStation.getName(),
                        toStation.getCode(),
                        fromStop.getDepartureTime() != null ? fromStop.getDepartureTime() : java.time.LocalTime.of(8, 0),
                        toStop.getArrivalTime() != null ? toStop.getArrivalTime() : java.time.LocalTime.of(20, 0),
                        Math.max(0, toStop.getDayOffset() - fromStop.getDayOffset()),
                        distanceKm,
                        availabilities
                );
                results.add(result);
            }
        }
        return results;
    }

    public Optional<Station> resolveStation(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanQuery = query.trim();
        // 1. Try code exact match
        Optional<Station> station = stationRepository.findByCodeIgnoreCase(cleanQuery);
        if (station.isPresent()) return station;

        // 2. Try name exact match
        station = stationRepository.findByNameIgnoreCase(cleanQuery);
        if (station.isPresent()) return station;

        // 3. Try search query substring match
        List<Station> searchResults = stationRepository.searchStations(cleanQuery);
        if (!searchResults.isEmpty()) {
            return Optional.of(searchResults.get(0));
        }

        // 4. Create on-demand station if code looks like a 3-4 letter Indian railway station code
        if (cleanQuery.length() >= 2 && cleanQuery.length() <= 5 && cleanQuery.matches("^[a-zA-Z]+$")) {
            Station newStation = new Station();
            newStation.setCode(cleanQuery.toUpperCase());
            newStation.setName(cleanQuery.toUpperCase());
            newStation.setCity(cleanQuery.toUpperCase());
            return Optional.of(stationRepository.save(newStation));
        }

        return Optional.empty();
    }

    private TrainRun getOrCreateTrainRun(Train train, LocalDate date) {
        Optional<TrainRun> existing = trainRunRepository.findByTrainAndRunDate(train, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        TrainRun newRun = new TrainRun();
        newRun.setTrain(train);
        newRun.setRunDate(date);
        newRun.setStatus(TrainRunStatus.SCHEDULED);
        trainRunRepository.save(newRun);
        
        List<TrainClassConfig> classConfigs = trainClassConfigRepository.findByTrain(train);
        for (TrainClassConfig config : classConfigs) {
            SeatInventory inventory = new SeatInventory();
            inventory.setTrainRun(newRun);
            inventory.setSeatClass(config.getSeatClass());
            inventory.setTotalSeats(config.getTotalSeats());
            inventory.setConfirmedBooked(0);
            inventory.setRacBooked(0);
            inventory.setRacQuota(config.getRacQuota());
            inventory.setWaitlistCount(0);
            seatInventoryRepository.save(inventory);
        }
        
        return newRun;
    }
}
