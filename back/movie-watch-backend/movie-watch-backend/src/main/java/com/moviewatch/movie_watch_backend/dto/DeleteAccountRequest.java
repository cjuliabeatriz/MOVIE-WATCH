package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.NotBlank;

// DTO de entrada para exclusão da própria conta.
// A senha é exigida para evitar exclusão acidental.
public record DeleteAccountRequest(
        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}