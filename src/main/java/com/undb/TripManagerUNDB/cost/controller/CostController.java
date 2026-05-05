package com.undb.TripManagerUNDB.cost.controller;

import com.undb.TripManagerUNDB.cost.entity.CostSummary;
import com.undb.TripManagerUNDB.cost.service.CostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    /**
     * Retorna o breakdown de custos.
     * Alimenta o donut chart e a lista de itens do painel direito do dashboard.
     */
    @GetMapping("/{tripId}")
    public CostSummary getCosts(@PathVariable String tripId) {
        return costService.getOrCalculate(tripId);
    }

    /**
     * Recalcula os custos — chamado após editar atividades do roteiro.
     */
    @PostMapping("/{tripId}/recalculate")
    public CostSummary recalculate(@PathVariable String tripId) {
        return costService.calculate(tripId);
    }

    /**
     * Retorna só a dica da IA — alimenta o InfoBox "Dica da IA" do dashboard.
     */
    @GetMapping("/{tripId}/tip")
    public Map<String, String> getTip(@PathVariable String tripId) {
        return Map.of("tip", costService.getTip(tripId));
    }
}
