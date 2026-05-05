package com.undb.TripManagerUNDB.auth.controller;

import com.undb.TripManagerUNDB.auth.dto.AuthRequest;
import com.undb.TripManagerUNDB.auth.dto.AuthResponse;
import com.undb.TripManagerUNDB.auth.dto.RegisterRequest;
import com.undb.TripManagerUNDB.auth.service.AuthService;
import com.undb.TripManagerUNDB.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * Retorna dados do usuário logado.
     * Alimenta o header da tela de perfil (nome, email, plano).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
                "id",    user.getId(),
                "name",  user.getName(),
                "email", user.getEmail(),
                "plan",  user.getPlan().name()
        ));
    }
}
