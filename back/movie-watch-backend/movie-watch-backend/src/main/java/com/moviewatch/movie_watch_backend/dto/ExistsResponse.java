package com.moviewatch.movie_watch_backend.dto;

// DTO de resposta booleana usado para indicar se um filme está favoritado.
public record ExistsResponse(boolean favorited) {
}