package com.railway.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = false)
public class DayOfWeekSet implements AttributeConverter<Set<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "DAILY";
        }
        return attribute.stream()
                .map(d -> d.name().substring(0, 3).toUpperCase())
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || dbData.equalsIgnoreCase("DAILY") || dbData.equalsIgnoreCase("ALL")) {
            return EnumSet.allOf(DayOfWeek.class);
        }
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String part : dbData.split(",")) {
            String clean = part.trim().toUpperCase();
            for (DayOfWeek d : DayOfWeek.values()) {
                if (d.name().startsWith(clean) || clean.startsWith(d.name().substring(0, 3))) {
                    days.add(d);
                    break;
                }
            }
        }
        return days.isEmpty() ? EnumSet.allOf(DayOfWeek.class) : days;
    }
}
