package com.railway.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentWebhookResponse {
    private boolean success;
    private String message;
    private String bookingStatus;
}
