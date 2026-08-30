package com.railway.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentInitiateResponse {
    private String transactionRef;
    private double amount;
    private String status;
    private String message;
}
