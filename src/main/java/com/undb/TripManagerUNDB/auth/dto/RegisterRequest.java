package com.undb.TripManagerUNDB.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nome obrigatório")
        String name,

        @Email(message = "E-mail inválido")
        @NotBlank(message = "E-mail obrigatório")
        String email,

        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
        String password
) {}
