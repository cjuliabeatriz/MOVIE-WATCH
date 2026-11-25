package com.moviewatch.movie_watch_backend.dto;

import java.time.LocalDateTime;

// DTO de saída com os dados públicos de um usuário.
// Nunca inclui senha, hash de senha ou token de recuperação.
public record UserResponse(
        Long id,
        String name,
        String email,
        Boolean blocked,
        LocalDateTime createdAt
) {
}