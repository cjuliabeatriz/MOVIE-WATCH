package com.moviewatch.movie_watch_backend.dto;

import jakarta.validation.constraints.NotNull;

// DTO de entrada para bloquear ou desbloquear uma conta.
public record BlockUserRequest(
        @NotNull(message = "Informe o estado de bloqueio.")
        Boolean blocked
) {
}