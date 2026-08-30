package com.railway.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BookingResponse {
    private String pnr;
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private LocalDate date;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String seatClassCode;
    private String seatClassLabel;
    private String status;
    private double totalFare;
    private boolean paid;
    private boolean boardable;
    private Integer waitlistPosition;
    private LocalDateTime holdExpiresAt;
    private String qrCodeDataUri;
    private List<PassengerResponse> passengers;
    private LocalDateTime createdAt;
}
