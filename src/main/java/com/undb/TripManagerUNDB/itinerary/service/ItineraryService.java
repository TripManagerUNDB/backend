package com.undb.TripManagerUNDB.itinerary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undb.TripManagerUNDB.itinerary.entity.ItineraryDay;
import com.undb.TripManagerUNDB.itinerary.repository.ItineraryRepository;
import com.undb.TripManagerUNDB.trip.entity.Trip;
import com.undb.TripManagerUNDB.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripService tripService;
    private final ObjectMapper objectMapper;

    @Value("${app.python-api.url:http://localhost:8000}")
    private String pythonApiUrl;

    private static final DateTimeFormatter BR_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM",
            new Locale("pt", "BR"));

    public List<ItineraryDay> generate(String userId, String tripId) {
        Trip trip = tripService.findEntityById(tripId);

        if (!trip.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Acesso negado.");
        }

        itineraryRepository.deleteByTripId(tripId);

        int days = (int) (trip.getCheckOut().toEpochDay() - trip.getCheckIn().toEpochDay());

        // Normaliza o destino removendo acentos antes de enviar para a API Python
        String destination = normalizeDestination(trip.getDestination());

        Map<String, Object> payload = new HashMap<>();
        payload.put("destination", destination);
        payload.put("days", days);
        payload.put("travelers", trip.getTravelers());
        payload.put("budget", trip.getBudgetLabel());
        payload.put("preferences", trip.getInterests());
        payload.put("travel_style", trip.getTravelStyle() != null ? trip.getTravelStyle() : "moderado");
        payload.put("mobility_restrictions", false);
        payload.put("accommodation", "hotel");

        log.info("Chamando API Python para tripId={} destino={}", tripId, destination);

        try {
            String body = objectMapper.writeValueAsString(payload);

            java.net.URL url = new java.net.URL(pythonApiUrl + "/trip/plan");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            int status = conn.getResponseCode();
            java.io.InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
            String response = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            conn.disconnect();

            if (status >= 400) {
                throw new RuntimeException("API Python retornou " + status + ": " + response);
            }

            log.info("Resposta recebida da API Python para tripId={}", tripId);
            return parseAndSave(response, tripId, trip.getCheckIn());

        } catch (Exception e) {
            log.error("Erro ao chamar API Python: {}. Usando gerador local.", e.getMessage());
            return generateFallback(tripId, trip);
        }
    }

    private List<ItineraryDay> parseAndSave(String json, String tripId, LocalDate checkIn) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode itinerary = root.get("itinerary");
        JsonNode allMapPins = root.get("map_pins");

        List<ItineraryDay> result = new ArrayList<>();

        for (JsonNode dayNode : itinerary) {
            int dayNum = dayNode.get("day").asInt();
            String title = dayNode.path("title").asText("");
            String dailyCost = dayNode.path("daily_cost_estimate").asText(null);
            LocalDate date = checkIn.plusDays(dayNum - 1);

            List<ItineraryDay.Activity> activities = new ArrayList<>();
            for (JsonNode act : dayNode.get("activities")) {
                String estimatedCostText = act.path("estimated_cost").asText("Gratuito");
                int costInt = parseCost(estimatedCostText);

                ItineraryDay.Coordinates coords = null;
                if (act.has("coordinates") && !act.get("coordinates").isNull()) {
                    coords = ItineraryDay.Coordinates.builder()
                            .lat(act.get("coordinates").get("lat").asDouble())
                            .lng(act.get("coordinates").get("lng").asDouble())
                            .build();
                }

                activities.add(ItineraryDay.Activity.builder()
                        .time(act.path("time").asText(""))
                        .name(act.path("activity").asText(""))
                        .location(act.path("location").asText(""))
                        .type(inferType(act.path("activity").asText("")))
                        .icon(inferIcon(act.path("activity").asText("")))
                        .dur("2h")
                        .cost(costInt)
                        .estimatedCost(estimatedCostText)
                        .desc(act.path("tips").asText(""))
                        .coordinates(coords)
                        .build());
            }

            List<ItineraryDay.MapPin> mapPins = new ArrayList<>();
            if (allMapPins != null) {
                for (JsonNode pin : allMapPins) {
                    if (pin.get("day").asInt() == dayNum) {
                        ItineraryDay.Coordinates coords = null;
                        if (pin.has("coordinates")) {
                            coords = ItineraryDay.Coordinates.builder()
                                    .lat(pin.get("coordinates").get("lat").asDouble())
                                    .lng(pin.get("coordinates").get("lng").asDouble())
                                    .build();
                        }
                        mapPins.add(ItineraryDay.MapPin.builder()
                                .day(dayNum)
                                .time(pin.path("time").asText(""))
                                .activity(pin.path("activity").asText(""))
                                .location(pin.path("location").asText(""))
                                .type(pin.path("type").asText(""))
                                .coordinates(coords)
                                .build());
                    }
                }
            }

            ItineraryDay day = ItineraryDay.builder()
                    .tripId(tripId)
                    .dayNumber(dayNum)
                    .date(date.format(BR_FORMAT))
                    .title(title)
                    .activities(activities)
                    .dailyCostEstimate(dailyCost)
                    .mapPins(mapPins)
                    .build();

            result.add(itineraryRepository.save(day));
        }

        log.info("Salvos {} dias de roteiro para tripId={}", result.size(), tripId);
        return result;
    }

    // ── Normaliza destino removendo acentos ───────────────────
    private String normalizeDestination(String dest) {
        return java.text.Normalizer
                .normalize(dest, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private int parseCost(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("Gratuito"))
            return 0;
        try {
            return (int) Double.parseDouble(
                    text.replaceAll("[^0-9,.]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String inferType(String activity) {
        String lower = activity.toLowerCase();
        if (lower.contains("restaurant") || lower.contains("café") || lower.contains("almoço")
                || lower.contains("jantar"))
            return "Restaurante";
        if (lower.contains("museu") || lower.contains("museum"))
            return "Museu";
        if (lower.contains("igreja") || lower.contains("catedral") || lower.contains("templo"))
            return "Igreja";
        if (lower.contains("palácio") || lower.contains("castelo"))
            return "Palácio";
        if (lower.contains("parque") || lower.contains("jardim") || lower.contains("praia"))
            return "Natureza";
        if (lower.contains("mercado") || lower.contains("shopping") || lower.contains("loja"))
            return "Compras";
        return "Passeio";
    }

    private String inferIcon(String activity) {
        String lower = activity.toLowerCase();
        if (lower.contains("restaurant") || lower.contains("jantar") || lower.contains("almoço"))
            return "🍽️";
        if (lower.contains("café") || lower.contains("coffee"))
            return "☕";
        if (lower.contains("museu"))
            return "🎨";
        if (lower.contains("igreja") || lower.contains("catedral"))
            return "⛪";
        if (lower.contains("palácio") || lower.contains("castelo"))
            return "🏰";
        if (lower.contains("parque") || lower.contains("jardim"))
            return "🌿";
        if (lower.contains("praia"))
            return "🏖️";
        if (lower.contains("mercado"))
            return "🛍️";
        if (lower.contains("torre"))
            return "🗼";
        return "📍";
    }

    private List<ItineraryDay> generateFallback(String tripId, Trip trip) {
        List<ItineraryDay> days = new ArrayList<>();
        LocalDate current = trip.getCheckIn();
        int dayNum = 1;

        while (!current.isAfter(trip.getCheckOut().minusDays(1))) {
            List<ItineraryDay.Activity> acts = List.of(
                    ItineraryDay.Activity.builder().time("09:00").name("Exploração local").type("Passeio").icon("🗺️")
                            .dur("3h").cost(0).desc("Explore o centro da cidade.").build(),
                    ItineraryDay.Activity.builder().time("13:00").name("Almoço típico").type("Restaurante").icon("🍽️")
                            .dur("1h").cost(80).desc("Experimente a gastronomia local.").build(),
                    ItineraryDay.Activity.builder().time("15:00").name("Ponto turístico principal").type("Monumento")
                            .icon("📍").dur("2h").cost(50).desc("Visite o principal atrativo da cidade.").build());

            ItineraryDay day = ItineraryDay.builder()
                    .tripId(tripId).dayNumber(dayNum).date(current.format(BR_FORMAT))
                    .title("Dia " + dayNum + " em " + trip.getDestination())
                    .activities(new ArrayList<>(acts)).build();

            days.add(itineraryRepository.save(day));
            current = current.plusDays(1);
            dayNum++;
        }
        return days;
    }

    public List<ItineraryDay> getDays(String tripId) {
        return itineraryRepository.findByTripIdOrderByDayNumber(tripId);
    }

    public ItineraryDay updateActivity(String dayId, int index, ItineraryDay.Activity updated) {
        ItineraryDay day = findDay(dayId);
        if (index < 0 || index >= day.getActivities().size())
            throw new IllegalArgumentException("Índice de atividade inválido.");
        day.getActivities().set(index, updated);
        return itineraryRepository.save(day);
    }

    public ItineraryDay removeActivity(String dayId, int index) {
        ItineraryDay day = findDay(dayId);
        if (index < 0 || index >= day.getActivities().size())
            throw new IllegalArgumentException("Índice de atividade inválido.");
        day.getActivities().remove(index);
        return itineraryRepository.save(day);
    }

    private ItineraryDay findDay(String dayId) {
        return itineraryRepository.findById(dayId)
                .orElseThrow(() -> new NoSuchElementException("Dia não encontrado: " + dayId));
    }
}
