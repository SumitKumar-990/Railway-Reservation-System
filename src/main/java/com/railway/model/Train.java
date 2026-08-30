package com.railway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "trainNumber")
@ToString(exclude = {"stops", "classConfigs"})
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trainNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String runningDays; // e.g. "MON,WED,FRI"

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TrainStop> stops = new HashSet<>();

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TrainClassConfig> classConfigs = new HashSet<>();

    public boolean runsOn(DayOfWeek day) {
        if (runningDays == null || runningDays.trim().isEmpty()) return true;
        String trimmed = runningDays.trim().toUpperCase();
        if (trimmed.equals("DAILY") || trimmed.equals("ALL") || trimmed.contains("DAILY")) return true;
        String dayStr = day.name().substring(0, 3).toUpperCase();
        return Arrays.stream(runningDays.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(d -> d.equals(dayStr) || d.startsWith(dayStr));
    }
}

