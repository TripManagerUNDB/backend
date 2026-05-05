package com.undb.TripManagerUNDB.itinerary.controller;

import com.undb.TripManagerUNDB.itinerary.entity.ItineraryDay;
import com.undb.TripManagerUNDB.itinerary.service.ItineraryService;
import com.undb.TripManagerUNDB.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    /**
     * Gera o roteiro completo para uma viagem.
     * Chamado pelo wizard ao clicar "Gerar Roteiro".
     * Body: { "tripId": "abc123" }
     */
    @PostMapping("/generate")
    public ResponseEntity<List<ItineraryDay>> generate(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        String tripId = body.get("tripId");
        return ResponseEntity.ok(itineraryService.generate(user.getId(), tripId));
    }

    /**
     * Retorna os dias do roteiro.
     * Alimenta o accordion da coluna esquerda do dashboard.
     */
    @GetMapping("/{tripId}/days")
    public List<ItineraryDay> getDays(@PathVariable String tripId) {
        return itineraryService.getDays(tripId);
    }

    /** Edita uma atividade pelo índice dentro do dia */
    @PutMapping("/{dayId}/activity/{index}")
    public ItineraryDay updateActivity(
            @PathVariable String dayId,
            @PathVariable int index,
            @RequestBody ItineraryDay.Activity activity) {
        return itineraryService.updateActivity(dayId, index, activity);
    }

    /** Remove uma atividade pelo índice */
    @DeleteMapping("/{dayId}/activity/{index}")
    public ItineraryDay removeActivity(
            @PathVariable String dayId,
            @PathVariable int index) {
        return itineraryService.removeActivity(dayId, index);
    }
}
