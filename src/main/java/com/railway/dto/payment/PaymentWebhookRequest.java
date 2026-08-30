package com.railway.dto.payment;

import lombok.Data;

@Data
public class PaymentWebhookRequest {
    private String transactionRef;
    private String status;
    private String pnr;
}
