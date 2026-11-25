package com.moviewatch.movie_watch_backend.dto;

// DTO de resposta da solicitação de recuperação.
// O campo token só é preenchido quando a propriedade
// moviewatch.security.expose-reset-token está habilitada
// (uso exclusivo em desenvolvimento/testes).
public record ForgotPasswordResponse(String message, String token) {
}