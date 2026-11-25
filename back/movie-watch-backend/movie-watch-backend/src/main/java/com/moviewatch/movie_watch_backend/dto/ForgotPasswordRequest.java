package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO de entrada para solicitar recuperação de senha.
public record ForgotPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        String email
) {
}