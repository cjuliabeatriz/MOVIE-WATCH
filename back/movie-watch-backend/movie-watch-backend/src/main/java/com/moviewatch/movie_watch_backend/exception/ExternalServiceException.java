package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando a Cinemeta (API externa) está indisponível.
// O handler global converte em HTTP 502.
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }
}