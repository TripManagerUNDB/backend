package com.undb.TripManagerUNDB.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token obrigatório") String refreshToken) {
}
