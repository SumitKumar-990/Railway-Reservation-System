package com.railway.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PassengerResponse {
    private String name;
    private int age;
    private String gender;
    private String seatNumber;
    private String berthPreference;
}
