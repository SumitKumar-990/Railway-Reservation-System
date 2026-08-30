package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"train_run_id", "seat_class_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"trainRun", "seatClass"})
public class SeatInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_run_id")
    private TrainRun trainRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id")
    private SeatClass seatClass;

    private int totalSeats;

    @Builder.Default
    private int confirmedBooked = 0;

    @Builder.Default
    private int racBooked = 0;

    private int racQuota;

    @Builder.Default
    private int waitlistCount = 0;

    public int availableConfirmed() {
        return totalSeats - confirmedBooked;
    }

    public int availableRac() {
        return racQuota - racBooked;
    }
}
