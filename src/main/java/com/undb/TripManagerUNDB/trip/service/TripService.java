package com.undb.TripManagerUNDB.trip.service;

import com.undb.TripManagerUNDB.trip.dto.TripRequest;
import com.undb.TripManagerUNDB.trip.dto.TripResponse;
import com.undb.TripManagerUNDB.trip.entity.Trip;
import com.undb.TripManagerUNDB.trip.enums.TripStatus;
import com.undb.TripManagerUNDB.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;

    private static final Map<String, String> EMOJI_MAP = Map.ofEntries(
            Map.entry("Paris", "🗼"),
            Map.entry("Tóquio", "⛩️"),
            Map.entry("Tokyo", "⛩️"),
            Map.entry("Islândia", "🌋"),
            Map.entry("Iceland", "🌋"),
            Map.entry("Marrocos", "🕌"),
            Map.entry("Morocco", "🕌"),
            Map.entry("Buenos Aires", "🥩"),
            Map.entry("Lisboa", "🏰"),
            Map.entry("Lisbon", "🏰"),
            Map.entry("Roma", "🏛️"),
            Map.entry("Rome", "🏛️"),
            Map.entry("Nova York", "🗽"),
            Map.entry("New York", "🗽"),
            Map.entry("Bangkok", "🛕"),
            Map.entry("Bali", "🌺"),
            Map.entry("Amsterdam", "🚲"),
            Map.entry("Cidade do Cabo", "🦁"));

    private static final List<String> COLORS = List.of(
            "#EA9940", "#307082", "#6CA3A2",
            "#EA9940", "#307082", "#6CA3A2");

    // Mapeia o budget numérico para o label que a API Python espera
    private static final String[] BUDGET_LABELS = { "baixo", "médio", "alto" };

    public TripResponse create(String userId, TripRequest req) {
        String emoji = EMOJI_MAP.entrySet().stream()
                .filter(e -> req.destination().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("✈️");

        int count = (int) tripRepository.countByUserId(userId);
        String color = COLORS.get(count % COLORS.size());
        String budgetLabel = BUDGET_LABELS[Math.min(req.budget(), 2)];

        Trip trip = Trip.builder()
                .userId(userId)
                .destination(req.destination())
                .checkIn(req.checkIn())
                .checkOut(req.checkOut())
                .budget(req.budget())
                .budgetLabel(budgetLabel)
                .travelers(req.travelers() > 0 ? req.travelers() : 1)
                .travelStyle(req.travelStyle() != null ? req.travelStyle() : "moderado")
                .interests(req.interests())
                .emoji(emoji)
                .color(color)
                .build();

        return TripResponse.from(tripRepository.save(trip));
    }

    public List<TripResponse> listByUser(String userId, String status) {
        if (status != null && !status.isBlank()) {
            TripStatus ts = TripStatus.valueOf(status.toUpperCase());
            return tripRepository
                    .findByUserIdAndStatusOrderByCreatedAtDesc(userId, ts)
                    .stream().map(TripResponse::from).toList();
        }
        return tripRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(TripResponse::from).toList();
    }

    public TripResponse getById(String userId, String tripId) {
        return TripResponse.from(findOwned(userId, tripId));
    }

    public void delete(String userId, String tripId) {
        tripRepository.delete(findOwned(userId, tripId));
    }

    public Trip findEntityById(String tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Viagem não encontrada: " + tripId));
    }

    private Trip findOwned(String userId, String tripId) {
        Trip trip = findEntityById(tripId);
        if (!trip.getUserId().equals(userId)) {
            throw new NoSuchElementException("Viagem não encontrada: " + tripId);
        }
        return trip;
    }
}
