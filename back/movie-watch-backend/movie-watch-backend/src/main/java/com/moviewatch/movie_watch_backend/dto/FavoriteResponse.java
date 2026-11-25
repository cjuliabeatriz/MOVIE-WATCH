package com.moviewatch.movie_watch_backend.dto;

import java.time.LocalDateTime;

// DTO de saída de um favorito.
public record FavoriteResponse(
        Long id,
        String movieId,
        LocalDateTime createdAt
) {
}