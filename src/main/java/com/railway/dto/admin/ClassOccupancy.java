package com.railway.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ClassOccupancy {
    private String classCode;
    private String classLabel;
    private int totalSeats;
    private int confirmedBooked;
    private int racBooked;
    private int racQuota;
    private int waitlistCount;
}
