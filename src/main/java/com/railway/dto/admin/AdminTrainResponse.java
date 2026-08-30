package com.railway.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTrainResponse {
    private Long id;
    private String trainNumber;
    private String name;
    private String runningDays;
    private int stopCount;
    private int classConfigCount;
}
