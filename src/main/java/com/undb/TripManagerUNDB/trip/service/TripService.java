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

    // Mesmo mapeamento de emojis usado no frontend
    private static final Map<String, String> EMOJI_MAP = Map.ofEntries(
            Map.entry("Paris",        "🗼"),
            Map.entry("Tóquio",       "⛩️"),
            Map.entry("Tokyo",        "⛩️"),
            Map.entry("Islândia",     "🌋"),
            Map.entry("Iceland",      "🌋"),
            Map.entry("Marrocos",     "🕌"),
            Map.entry("Morocco",      "🕌"),
            Map.entry("Buenos Aires", "🥩"),
            Map.entry("Lisboa",       "🏰"),
            Map.entry("Lisbon",       "🏰"),
            Map.entry("Roma",         "🏛️"),
            Map.entry("Rome",         "🏛️"),
            Map.entry("Nova York",    "🗽"),
            Map.entry("New York",     "🗽"),
            Map.entry("Bangkok",      "🛕"),
            Map.entry("Bali",         "🌺"),
            Map.entry("Amsterdam",    "🚲"),
            Map.entry("Cidade do Cabo","🦁")
    );

    private static final List<String> COLORS = List.of(
            "#EA9940", "#307082", "#6CA3A2",
            "#EA9940", "#307082", "#6CA3A2"
    );

    public TripResponse create(String userId, TripRequest req) {
        String emoji = EMOJI_MAP.entrySet().stream()
                .filter(e -> req.destination().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("✈️");

        int count = (int) tripRepository.countByUserId(userId);
        String color = COLORS.get(count % COLORS.size());

        Trip trip = Trip.builder()
                .userId(userId)
                .destination(req.destination())
                .checkIn(req.checkIn())
                .checkOut(req.checkOut())
                .budget(req.budget())
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

    /** Usado internamente por outros serviços */
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
