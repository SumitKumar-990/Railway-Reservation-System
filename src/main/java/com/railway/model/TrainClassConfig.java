package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "train_class_configs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"train_id", "seat_class_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"train", "seatClass"})
public class TrainClassConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_class_id")
    private SeatClass seatClass;

    private int totalSeats;
    private int racQuota;
    private double farePerKm;
    private double baseFare;
}
