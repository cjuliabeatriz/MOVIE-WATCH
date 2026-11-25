package com.moviewatch.movie_watch_backend.exception;

// Exceção lançada quando o usuário autenticado tenta acessar
// um recurso que pertence a outro usuário.
// O handler global converte em HTTP 403.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}