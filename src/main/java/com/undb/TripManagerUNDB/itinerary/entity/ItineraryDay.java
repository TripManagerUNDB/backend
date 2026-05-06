package com.undb.TripManagerUNDB.itinerary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "itinerary_days")
public class ItineraryDay {

    @Id
    private String id;

    @Indexed
    private String tripId;

    private int dayNumber;
    private String date; // "Seg, 15 Jun"
    private String title; // "Dia cultural em Paris" — vem da API

    private List<Activity> activities;

    /** Custo total estimado do dia — vem da API */
    private String dailyCostEstimate;

    /** Pins do mapa deste dia — vem da API */
    private List<MapPin> mapPins;

    // ── Activity ──────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String time; // "09:00"
        private String name; // "Torre Eiffel" — mapeado de activity
        private String location; // nome do local exato
        private String type; // "Monumento"
        private String icon; // "🗼"
        private String dur; // "2h"
        private int cost; // 120 — parseado de estimated_cost
        private String estimatedCost; // "R$ 120,00" — texto original da API
        private String desc; // tips da API
        private Coordinates coordinates;
    }

    // ── MapPin ────────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapPin {
        private int day;
        private String time;
        private String activity;
        private String location;
        private String type;
        private Coordinates coordinates;
    }

    // ── Coordinates ───────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinates {
        private double lat;
        private double lng;
    }
}
