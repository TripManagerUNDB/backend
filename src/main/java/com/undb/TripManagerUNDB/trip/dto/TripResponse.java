package com.undb.TripManagerUNDB.trip.dto;

import com.undb.TripManagerUNDB.trip.entity.Trip;

import java.time.LocalDate;
import java.util.List;

/** Resposta usada nos cards da tela de perfil */
public record TripResponse(
        String id,
        String destination,
        LocalDate checkIn,
        LocalDate checkOut,
        int budget,
        List<String> interests,
        String status,
        String emoji,
        String color,
        int days
) {
    public static TripResponse from(Trip t) {
        int days = (t.getCheckIn() != null && t.getCheckOut() != null)
                ? (int) (t.getCheckOut().toEpochDay() - t.getCheckIn().toEpochDay())
                : 0;
        return new TripResponse(
                t.getId(), t.getDestination(),
                t.getCheckIn(), t.getCheckOut(),
                t.getBudget(), t.getInterests(),
                t.getStatus().name(),
                t.getEmoji(), t.getColor(),
                days
        );
    }
}
