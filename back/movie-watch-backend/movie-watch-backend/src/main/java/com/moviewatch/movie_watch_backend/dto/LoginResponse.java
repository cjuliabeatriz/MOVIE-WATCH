package com.moviewatch.movie_watch_backend.dto;

// DTO de resposta do login.
// Contém o token Bearer e os dados públicos do usuário.
public record LoginResponse(String token, UserResponse user) {
}