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

    /** ex: "Seg, 15 Jun" — mesmo formato do frontend */
    private String date;

    private List<Activity> activities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String time;   // "09:00"
        private String name;   // "Torre Eiffel"
        private String type;   // "Monumento"
        private String icon;   // "🗼"
        private String dur;    // "2h"
        private int    cost;   // 120
        private String desc;   // dica local
    }
}
