package com.moviewatch.movie_watch_backend.dto;

// DTO genérico de resposta textual.
// Usado em operações que retornam apenas uma mensagem de sucesso.
public record MessageResponse(String message) {
}