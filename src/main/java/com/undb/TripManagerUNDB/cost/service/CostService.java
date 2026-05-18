package com.undb.TripManagerUNDB.cost.service;

import com.undb.TripManagerUNDB.cost.entity.CostSummary;
import com.undb.TripManagerUNDB.cost.repository.CostRepository;
import com.undb.TripManagerUNDB.itinerary.entity.ItineraryDay;
import com.undb.TripManagerUNDB.itinerary.repository.ItineraryRepository;
import com.undb.TripManagerUNDB.trip.entity.Trip;
import com.undb.TripManagerUNDB.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CostService {

    private final CostRepository costRepository;
    private final ItineraryRepository itineraryRepository;
    private final TripService tripService;

    // Cores iguais às do frontend (donut chart)
    private static final String[] COLORS = { "#EA9940", "#307082", "#6CA3A2", "#ECE7DC", "#243545" };

    // Multiplicadores por nível de budget (0=eco, 1=confortável, 2=luxo)
    private static final int[] MULT = { 1, 2, 4 };

    public CostSummary getOrCalculate(String tripId) {
        return costRepository.findFirstByTripId(tripId).orElseGet(() -> calculate(tripId));
    }

    public CostSummary calculate(String tripId) {
        Trip trip = tripService.findEntityById(tripId);
        List<ItineraryDay> days = itineraryRepository.findByTripIdOrderByDayNumber(tripId);

        int nights = (int) (trip.getCheckOut().toEpochDay() - trip.getCheckIn().toEpochDay());
        int m = MULT[Math.min(trip.getBudget(), 2)];

        // Soma o custo real das atividades
        int actTotal = days.stream()
                .flatMap(d -> d.getActivities().stream())
                .mapToInt(ItineraryDay.Activity::getCost)
                .sum();

        int voo = 800 * m;
        int hotel = 200 * m * nights;
        int alimentacao = Math.max(actTotal / 2, 100 * m * nights);
        int transporte = 50 * m * nights;
        int atracoes = actTotal > 0 ? actTotal : 80 * m * nights;
        int total = voo + hotel + alimentacao + transporte + atracoes;

        List<CostSummary.CostItem> breakdown = List.of(
                item("Voo", voo, total, COLORS[0]),
                item("Hotel", hotel, total, COLORS[1]),
                item("Alimentação", alimentacao, total, COLORS[2]),
                item("Transporte", transporte, total, COLORS[3]),
                item("Atrações", atracoes, total, COLORS[4]));

        String tip = buildTip(trip, m);

        CostSummary summary = CostSummary.builder()
                .tripId(tripId)
                .total(total)
                .breakdown(breakdown)
                .tip(tip)
                .build();

        // Upsert: preserva o id se já existir
        costRepository.findFirstByTripId(tripId).ifPresent(old -> summary.setId(old.getId()));
        return costRepository.save(summary);
    }

    public String getTip(String tripId) {
        return getOrCalculate(tripId).getTip();
    }

    // ── helpers ──────────────────────────────────────────────

    private CostSummary.CostItem item(String label, int value, int total, String color) {
        int pct = total > 0 ? Math.round((float) value / total * 100) : 0;
        return CostSummary.CostItem.builder()
                .label(label).value(value).pct(pct).color(color).build();
    }

    private String buildTip(Trip trip, int mult) {
        int month = trip.getCheckIn().getMonthValue();
        String dest = trip.getDestination();

        // Dicas específicas por destino
        if (dest.contains("Paris") || dest.contains("Roma") || dest.contains("Amsterdam")) {
            if (month >= 6 && month <= 8) {
                return "Alta temporada europeia: hotéis sobem 30-40% em julho. Reserve com 60+ dias de antecedência e considere voos nas terças ou quartas.";
            }
            if (month == 5 || month == 9) {
                return "Ótima época! Maio e setembro têm clima agradável com preços até 25% menores que o verão. Ideal para museus sem filas longas.";
            }
        }
        if (dest.contains("Tóquio") || dest.contains("Tokyo")) {
            if (month == 3 || month == 4) {
                return "Temporada das cerejeiras (março-abril): preços sobem 40%. Reserve hospedagem com 90+ dias de antecedência para encontrar boas tarifas.";
            }
            return "Junho-agosto é quente e úmido em Tóquio. Considere novembro — temperatura agradável e folhagem outonal belíssima.";
        }
        if (dest.contains("Islândia") || dest.contains("Iceland")) {
            if (month >= 6 && month <= 8) {
                return "Sol da meia-noite no verão: paisagens incríveis mas preços altos. Aluguel de carro é essencial — evite agências do aeroporto.";
            }
            return "Inverno (nov-mar): melhor época para ver aurora boreal e preços 35% mais baixos. Reserve Aurora Forecast para rastrear as luzes.";
        }

        // Dica genérica por época
        if (month >= 6 && month <= 8) {
            return "Viajando na alta temporada: reserve voo e hotel com pelo menos 45 dias de antecedência. Preços sobem significativamente nesse período.";
        }
        if (month >= 11 || month <= 2) {
            return "Baixa temporada: excelente custo-benefício. Hotéis até 30% mais baratos e atrações sem filas. Verifique horários de funcionamento antecipadamente.";
        }
        return "Período intermediário com bom equilíbrio de preços e clima. Reserve voos com 30+ dias de antecedência para melhores tarifas.";
    }
}
