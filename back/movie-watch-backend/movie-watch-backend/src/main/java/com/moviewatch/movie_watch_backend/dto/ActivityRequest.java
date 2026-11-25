package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de entrada para registrar uma interação manual do usuário.
// O tipo de atividade é validado contra uma lista permitida no service.
public record ActivityRequest(
        @NotBlank(message = "O tipo de atividade é obrigatório.")
        @Size(max = 50, message = "O tipo de atividade deve ter no máximo 50 caracteres.")
        String activityType,

        @Size(max = 255, message = "O identificador do filme deve ter no máximo 255 caracteres.")
        String movieId
) {
}