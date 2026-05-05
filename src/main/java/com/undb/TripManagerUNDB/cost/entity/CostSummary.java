package com.undb.TripManagerUNDB.cost.entity;

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
@Document(collection = "cost_summaries")
public class CostSummary {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tripId;

    private int total;

    private List<CostItem> breakdown;

    /** Dica gerada para o InfoBox do dashboard */
    private String tip;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostItem {
        private String label;   // "Voo"
        private int    value;   // 3200
        private int    pct;     // 38
        private String color;   // "#EA9940"
    }
}
