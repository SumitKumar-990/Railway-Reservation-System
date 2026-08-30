package com.railway.dto.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull
    private Long trainRunId;
    @NotBlank
    private String trainNumber;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @NotBlank
    private String fromStationCode;
    @NotBlank
    private String toStationCode;
    @NotBlank
    private String seatClassCode;
    @NotEmpty
    private List<PassengerRequest> passengers;
}
