package com.railway.controller;

import com.railway.dto.booking.BookingRequest;
import com.railway.dto.booking.BookingResponse;
import com.railway.model.User;
import com.railway.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.createBooking(request, user));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getUserBookings(user));
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String pnr, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getBooking(pnr, user));
    }

    @DeleteMapping("/{pnr}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String pnr, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.cancelBooking(pnr, user));
    }
}
