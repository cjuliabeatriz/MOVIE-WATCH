package com.moviewatch.movie_watch_backend.security;

import com.moviewatch.movie_watch_backend.config.MovieWatchProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testes unitários do TokenService.
class TokenServiceTest {

    private static final String SECRET = "chave-de-teste-com-mais-de-32-caracteres";

    // Cria um TokenService com segredo e expiração específicos.
    private TokenService service(String secret, long minutes) {
        MovieWatchProperties properties = new MovieWatchProperties();
        properties.getSecurity().setTokenSecret(secret);
        properties.getSecurity().setTokenExpirationMinutes(minutes);
        return new TokenService(properties);
    }

    @Test
    void deveEmitirTokenValidoComIdDoUsuario() {
        TokenService tokenService = service(SECRET, 60);

        String token = tokenService.issue(42L);
        Optional<Long> userId = tokenService.verify(token);

        assertTrue(userId.isPresent());
        assertEquals(42L, userId.get());
    }

    @Test
    void deveRejeitarTokenAlterado() {
        TokenService tokenService = service(SECRET, 60);
        String token = tokenService.issue(42L);

        // Altera o último caractere para simular adulteração.
        char ultimo = token.charAt(token.length() - 1);
        String adulterado = token.substring(0, token.length() - 1)
                + (ultimo == 'A' ? 'B' : 'A');

        assertTrue(tokenService.verify(adulterado).isEmpty());
    }

    @Test
    void deveRejeitarTokenAssinadoComOutroSegredo() {
        TokenService emissor = service(SECRET, 60);
        TokenService validador = service("outra-chave-de-teste-com-mais-de-32-caracteres", 60);

        String token = emissor.issue(42L);

        assertTrue(validador.verify(token).isEmpty());
    }

    @Test
    void deveRejeitarTokenMalformado() {
        TokenService tokenService = service(SECRET, 60);

        assertTrue(tokenService.verify("abc").isEmpty());
        assertTrue(tokenService.verify("").isEmpty());
        assertTrue(tokenService.verify(null).isEmpty());
    }

    @Test
    void deveRejeitarTokenExpirado() throws InterruptedException {
        TokenService tokenService = service(SECRET, 0);
        String token = tokenService.issue(42L);

        // Aguarda passar a validade mínima de 1 segundo.
        Thread.sleep(1200L);

        assertTrue(tokenService.verify(token).isEmpty());
    }

    @Test
    void deveGerarSegredoAleatorioQuandoNaoConfigurado() {
        TokenService tokenService = new TokenService(new MovieWatchProperties());
        String token = tokenService.issue(7L);

        assertTrue(tokenService.verify(token).isPresent());
    }
}