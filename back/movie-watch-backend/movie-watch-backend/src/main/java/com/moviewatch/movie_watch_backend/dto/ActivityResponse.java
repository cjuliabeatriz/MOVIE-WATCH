package com.moviewatch.movie_watch_backend.dto;

import java.time.LocalDateTime;

// DTO de saída de uma atividade do histórico.
public record ActivityResponse(
        Long id,
        String activityType,
        String movieId,
        LocalDateTime createdAt
) {
}