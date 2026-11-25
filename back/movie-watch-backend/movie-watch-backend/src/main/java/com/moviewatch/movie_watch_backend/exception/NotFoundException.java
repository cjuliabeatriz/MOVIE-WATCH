package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando o recurso solicitado não existe.
// O handler global converte em HTTP 404.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}