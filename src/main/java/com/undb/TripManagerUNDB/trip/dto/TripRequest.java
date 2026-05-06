package com.undb.TripManagerUNDB.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Payload enviado pelo wizard (etapas 1-4) */
public record TripRequest(
                @NotBlank String destination,
                @NotNull LocalDate checkIn,
                @NotNull LocalDate checkOut,
                int budget,
                List<String> interests,
                int travelers,
                String travelStyle) {
}
