package com.railway.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentInitiateRequest {
    @NotBlank
    private String pnr;
    @NotBlank
    private String method;
}
