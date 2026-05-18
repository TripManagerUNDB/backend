package com.undb.TripManagerUNDB.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.undb.TripManagerUNDB.trip.dto.TripRequest;
import com.undb.TripManagerUNDB.trip.dto.TripResponse;
import com.undb.TripManagerUNDB.trip.service.TripService;
import com.undb.TripManagerUNDB.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final ObjectMapper objectMapper;

    @Value("${app.python-api.url:http://localhost:8000}")
    private String pythonApiUrl;

    @GetMapping
    public List<TripResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status) {
        return tripService.listByUser(user.getId(), status);
    }

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

    @GetMapping("/validate-destination")
    public ResponseEntity<Map<String, Object>> validateDestination(
            @RequestParam String destination) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("destination", destination));

            System.out.println("DEBUG - PYTHON API URL: " + pythonApiUrl);
            System.out.println("DEBUG - REQUEST BODY: " + body);

            HttpClient client = HttpClient.newBuilder()
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pythonApiUrl + "/validate-destination"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG - RESPONSE STATUS: " + response.statusCode());
            System.out.println("DEBUG - RESPONSE BODY: " + response.body());

            if (response.statusCode() >= 400) {
                Map<String, Object> err = new java.util.HashMap<>();
                err.put("valid", false);
                err.put("message", "Erro ao validar destino.");
                return ResponseEntity.ok(err);
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> err = new java.util.HashMap<>();
            err.put("valid", false);
            err.put("message", e.getMessage());
            return ResponseEntity.ok(err);
        }
    }
}
