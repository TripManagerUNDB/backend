package com.undb.TripManagerUNDB.trip.controller;

import com.undb.TripManagerUNDB.trip.dto.TripRequest;
import com.undb.TripManagerUNDB.trip.dto.TripResponse;
import com.undb.TripManagerUNDB.trip.service.TripService;
import com.undb.TripManagerUNDB.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * Lista viagens do usuário logado.
     * ?status=PLANEJADA | CONCLUIDA para filtrar (tela de perfil)
     */
    @GetMapping
    public List<TripResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status) {
        return tripService.listByUser(user.getId(), status);
    }

    /** Cria viagem a partir do payload do wizard */
    @PostMapping
    public ResponseEntity<TripResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TripRequest req) {
        return ResponseEntity.status(201).body(tripService.create(user.getId(), req));
    }

    @GetMapping("/{id}")
    public TripResponse getById(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        return tripService.getById(user.getId(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        tripService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestParam String status) {
        return ResponseEntity.ok(tripService.updateStatus(user.getId(), id, status));
    }
}
