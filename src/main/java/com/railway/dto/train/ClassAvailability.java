package com.railway.dto.train;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ClassAvailability {
    private String classCode;
    private String classLabel;
    private int availableSeats;
    private int racAvailable;
    private int waitlistCount;
    private double fare;
    private String statusLabel; // AVAILABLE, RAC, WAITLIST, FULL
}
