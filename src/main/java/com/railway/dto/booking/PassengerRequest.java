package com.railway.dto.booking;

import com.railway.model.BerthPreference;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PassengerRequest {
    @NotBlank
    private String name;
    private int age;
    private String gender;
    private BerthPreference berthPreference;
}
