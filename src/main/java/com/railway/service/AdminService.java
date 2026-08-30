package com.railway.service;

import com.railway.dto.admin.AdminTrainResponse;
import com.railway.dto.admin.ClassOccupancy;
import com.railway.dto.admin.TrainRunOccupancyResponse;
import com.railway.exception.ResourceNotFoundException;
import com.railway.model.SeatInventory;
import com.railway.model.Train;
import com.railway.model.TrainRun;
import com.railway.model.TrainRunStatus;
import com.railway.repository.BookingRepository;
import com.railway.repository.SeatInventoryRepository;
import com.railway.repository.TrainRepository;
import com.railway.repository.TrainRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TrainRepository trainRepository;
    private final TrainRunRepository trainRunRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public TrainRunOccupancyResponse getTrainRunOccupancy(Long trainRunId) {
        TrainRun run = trainRunRepository.findById(trainRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Train run with id " + trainRunId + " not found"));
        List<SeatInventory> inventories = seatInventoryRepository.findByTrainRun(run);
        
        List<ClassOccupancy> classOccupancies = new ArrayList<>();
        for (SeatInventory inv : inventories) {
            classOccupancies.add(new ClassOccupancy(
                    inv.getSeatClass().getCode(),
                    inv.getSeatClass().getLabel(),
                    inv.getTotalSeats(),
                    inv.getConfirmedBooked(),
                    inv.getRacBooked(),
                    inv.getRacQuota(),
                    inv.getWaitlistCount()
            ));
        }

        return new TrainRunOccupancyResponse(
                run.getTrain().getTrainNumber(),
                run.getTrain().getName(),
                run.getRunDate(),
                run.getStatus().name(),
                classOccupancies
        );
    }

    @Transactional
    public void cancelTrainRun(Long trainRunId) {
        TrainRun run = trainRunRepository.findById(trainRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Train run with id " + trainRunId + " not found"));
        run.setStatus(TrainRunStatus.CANCELLED);
        trainRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public List<AdminTrainResponse> getAllTrains() {
        return trainRepository.findAll().stream()
                .map(t -> AdminTrainResponse.builder()
                        .id(t.getId())
                        .trainNumber(t.getTrainNumber())
                        .name(t.getName())
                        .runningDays(t.getRunningDays())
                        .stopCount(t.getStops() != null ? t.getStops().size() : 0)
                        .classConfigCount(t.getClassConfigs() != null ? t.getClassConfigs().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }
}
