package com.undb.TripManagerUNDB.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String name,
        String email,
        String plan
) {}
