package com.undb.TripManagerUNDB.trip.entity;

import com.undb.TripManagerUNDB.trip.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String destination;
    private LocalDate checkIn;
    private LocalDate checkOut;

    /** 0 = Econômico | 1 = Confortável | 2 = Luxo */
    private int budget;

    /** ex: ["praia", "cultura", "gastro"] */
    private List<String> interests;

    @Builder.Default
    private TripStatus status = TripStatus.PLANEJADA;

    /** Emoji exibido no card da tela de perfil */
    private String emoji;

    /** Cor do card no perfil */
    private String color;

    @CreatedDate
    private Instant createdAt;
}
