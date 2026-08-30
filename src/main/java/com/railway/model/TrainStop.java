package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "train_stops", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"train_id", "sequence_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"train", "station"})
public class TrainStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    @Builder.Default
    private int dayOffset = 0;

    @Column(nullable = false)
    private int distanceFromOriginKm;
}
