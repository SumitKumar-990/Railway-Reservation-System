package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "pnr")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pnr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_run_id")
    private TrainRun trainRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_station_id")
    private Station fromStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_station_id")
    private Station toStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id")
    private SeatClass seatClass;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private double totalFare;

    @Builder.Default
    private boolean paid = false;

    private LocalDateTime holdExpiresAt;
    
    private Integer waitlistPosition;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isBoardable() {
        return paid && (status == BookingStatus.CONFIRMED || status == BookingStatus.RAC);
    }
}
