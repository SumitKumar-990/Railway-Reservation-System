package com.railway.controller;

import com.railway.dto.payment.PaymentInitiateRequest;
import com.railway.dto.payment.PaymentInitiateResponse;
import com.railway.dto.payment.PaymentWebhookRequest;
import com.railway.dto.payment.PaymentWebhookResponse;
import com.railway.model.User;
import com.railway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(@RequestBody PaymentInitiateRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, user));
    }

    @PostMapping("/webhook")
    public ResponseEntity<PaymentWebhookResponse> processWebhook(@RequestBody PaymentWebhookRequest request) {
        return ResponseEntity.ok(paymentService.processWebhook(request));
    }
}
