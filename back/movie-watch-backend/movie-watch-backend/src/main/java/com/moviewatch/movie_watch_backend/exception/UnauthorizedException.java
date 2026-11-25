package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando as credenciais estão incorretas.
// O handler global converte em HTTP 401.
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}