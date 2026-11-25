package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando há conflito de estado,
// como e-mail duplicado ou favorito já cadastrado.
// O handler global converte em HTTP 409.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}