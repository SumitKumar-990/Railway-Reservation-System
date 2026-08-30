package com.railway.service;

import com.railway.dto.payment.PaymentInitiateRequest;
import com.railway.dto.payment.PaymentInitiateResponse;
import com.railway.dto.payment.PaymentWebhookRequest;
import com.railway.dto.payment.PaymentWebhookResponse;
import com.railway.exception.PaymentException;
import com.railway.model.Booking;
import com.railway.model.BookingStatus;
import com.railway.model.Payment;
import com.railway.model.PaymentStatus;
import com.railway.model.User;
import com.railway.repository.BookingRepository;
import com.railway.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request, User user) {
        Booking booking = bookingRepository.findByPnr(request.getPnr())
                .orElseThrow(() -> new PaymentException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new PaymentException("Not authorized");
        }

        if (booking.isPaid()) {
            throw new PaymentException("Booking is already paid");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new PaymentException("Booking is cancelled or expired");
        }

        if (booking.getHoldExpiresAt() != null && LocalDateTime.now().isAfter(booking.getHoldExpiresAt())) {
            throw new PaymentException("Booking hold has expired");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalFare());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setMethod(request.getMethod());
        payment.setGatewayTransactionRef("TXN-" + UUID.randomUUID().toString());
        payment.setInitiatedAt(LocalDateTime.now());
        
        paymentRepository.save(payment);

        return new PaymentInitiateResponse(
                payment.getGatewayTransactionRef(),
                payment.getAmount(),
                payment.getStatus().name(),
                "Payment initiated"
        );
    }

    @Transactional
    public PaymentWebhookResponse processWebhook(PaymentWebhookRequest request) {
        Payment payment = paymentRepository.findByGatewayTransactionRef(request.getTransactionRef())
                .orElseThrow(() -> new PaymentException("Payment not found"));

        Booking booking = payment.getBooking();

        if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            booking.setPaid(true);
            booking.setHoldExpiresAt(null);
            
            paymentRepository.save(payment);
            bookingRepository.save(booking);
            
            return new PaymentWebhookResponse(true, "Payment successful", booking.getStatus().name());
        } else if ("FAILED".equalsIgnoreCase(request.getStatus())) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            
            return new PaymentWebhookResponse(false, "Payment failed", booking.getStatus().name());
        }
        
        throw new PaymentException("Unknown payment status: " + request.getStatus());
    }
}
