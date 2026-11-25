package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de entrada para adicionar um filme aos favoritos.
// movieId é o identificador externo do filme no Cinemeta.
public record FavoriteRequest(
        @NotBlank(message = "O identificador do filme é obrigatório.")
        @Size(max = 255, message = "O identificador do filme deve ter no máximo 255 caracteres.")
        String movieId
) {
}