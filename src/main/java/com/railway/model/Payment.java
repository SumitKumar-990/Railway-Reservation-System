package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "booking")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String method;
    private String gatewayTransactionRef;

    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
}
