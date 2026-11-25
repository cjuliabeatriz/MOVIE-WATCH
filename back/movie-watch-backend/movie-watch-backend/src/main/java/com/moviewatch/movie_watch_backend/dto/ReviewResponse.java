package com.moviewatch.movie_watch_backend.dto;

import java.time.LocalDateTime;

// DTO de saída de uma avaliação.
// Inclui o nome do autor para que o frontend possa exibir
// os comentários sem expor a entidade User.
public record ReviewResponse(
        Long id,
        Long userId,
        String userName,
        String movieId,
        Short rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}