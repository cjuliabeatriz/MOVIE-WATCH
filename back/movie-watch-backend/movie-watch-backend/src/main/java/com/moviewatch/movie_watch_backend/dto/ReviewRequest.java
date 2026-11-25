package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de entrada para criar ou editar uma avaliação.
// A nota é opcional; quando ausente, o comentário é obrigatório.
public record ReviewRequest(
        @NotBlank(message = "O identificador do filme é obrigatório.")
        @Size(max = 255, message = "O identificador do filme deve ter no máximo 255 caracteres.")
        String movieId,

        @Min(value = 1, message = "A nota mínima é 1.")
        @Max(value = 5, message = "A nota máxima é 5.")
        Short rating,

        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres.")
        String comment
) {
}