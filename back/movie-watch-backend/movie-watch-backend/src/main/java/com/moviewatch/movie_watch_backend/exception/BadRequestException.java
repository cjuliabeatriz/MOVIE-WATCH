package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando uma requisição contém dados inválidos.
// O handler global converte em HTTP 400.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}