package com.railway.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TrainRunOccupancyResponse {
    private String trainNumber;
    private String trainName;
    private LocalDate runDate;
    private String status;
    private List<ClassOccupancy> classOccupancies;
}
