package com.moviewatch.movie_watch_backend.security;

import com.moviewatch.movie_watch_backend.config.MovieWatchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

// Serviço responsável por criar e validar os tokens de autenticação.
//
// O token tem o formato: base64(userId.expiraEm) . assinatura
// A assinatura é HMAC-SHA256, portanto qualquer alteração
// no conteúdo invalida o token.
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] secret;
    private final long expirationSeconds;

    public TokenService(MovieWatchProperties properties) {
        String configured = properties.getSecurity().getTokenSecret();

        if (configured == null || configured.isBlank()) {
            byte[] randomSecret = new byte[32];
            new SecureRandom().nextBytes(randomSecret);
            this.secret = randomSecret;

            log.warn("Segredo de token nao configurado. Um segredo aleatorio foi gerado; "
                    + "os tokens deixarao de valer apos reiniciar a aplicacao.");
        } else {
            this.secret = configured.getBytes(StandardCharsets.UTF_8);
        }

        this.expirationSeconds = Math.max(1L, properties.getSecurity().getTokenExpirationMinutes() * 60L);
    }

    // Cria um token assinado para o usuário informado.
    public String issue(Long userId) {
        long now = Instant.now().getEpochSecond();
        long expiresAt = now + expirationSeconds;

        String payload = userId + "." + expiresAt;
        String encodedPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        return encodedPayload + "." + sign(encodedPayload);
    }

    // Valida o token e devolve o ID do usuário quando ele é válido.
    public Optional<Long> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");

        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split("\\.");

            if (payloadParts.length != 2) {
                return Optional.empty();
            }

            long userId = Long.parseLong(payloadParts[0]);
            long expiresAt = Long.parseLong(payloadParts[1]);

            if (expiresAt <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }

            String expectedSignature = sign(parts[0]);
            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8)
            );

            return valid ? Optional.of(userId) : Optional.empty();
        } catch (RuntimeException ex) {
            // Token malformado, assinatura inválida ou base64 inválido.
            return Optional.empty();
        }
    }

    // Assina o conteúdo com HMAC-SHA256 e codifica em Base64 URL.
    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Nao foi possivel assinar o token.", ex);
        }
    }
}