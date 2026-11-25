package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// DTO de entrada para editar uma avaliação existente.
// O filme não pode ser alterado; apenas nota e comentário.
public record UpdateReviewRequest(
        @Min(value = 1, message = "A nota mínima é 1.")
        @Max(value = 5, message = "A nota máxima é 5.")
        Short rating,

        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres.")
        String comment
) {
}