package com.railway.dto.train;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainSearchRequest {
    private String from;
    private String to;
    private LocalDate date;
}
