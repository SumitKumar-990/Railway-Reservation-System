package com.railway.service;

import com.railway.model.TrainClassConfig;
import com.railway.model.TrainStop;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculator {

    public double calculateFare(TrainClassConfig config, TrainStop fromStop, TrainStop toStop, int passengerCount) {
        int distance = Math.abs(toStop.getDistanceFromOriginKm() - fromStop.getDistanceFromOriginKm());
        double farePerPassenger = config.getBaseFare() + (distance * config.getFarePerKm());
        double totalFare = farePerPassenger * passengerCount;
        
        BigDecimal bd = BigDecimal.valueOf(totalFare);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
