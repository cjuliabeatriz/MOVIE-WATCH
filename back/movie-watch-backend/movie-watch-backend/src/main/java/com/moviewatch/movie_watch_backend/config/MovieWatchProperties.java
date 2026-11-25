package com.moviewatch.movie_watch_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Agrupa as configurações próprias do Movie Watch.
// Os valores vêm do application.properties e podem ser
// sobrescritos por variáveis de ambiente.
@ConfigurationProperties(prefix = "moviewatch")
public class MovieWatchProperties {

    private final Security security = new Security();
    private final Cors cors = new Cors();
    private final Cinemeta cinemeta = new Cinemeta();

    // E-mails que possuem permissões administrativas.
    private Set<String> adminEmails = new LinkedHashSet<>();

    public Security getSecurity() {
        return security;
    }

    public Cors getCors() {
        return cors;
    }

    public Cinemeta getCinemeta() {
        return cinemeta;
    }

    public Set<String> getAdminEmails() {
        return adminEmails;
    }

    // Normaliza os e-mails administrativos para minúsculas.
    public void setAdminEmails(Set<String> adminEmails) {
        this.adminEmails = new LinkedHashSet<>();

        if (adminEmails == null) {
            return;
        }

        adminEmails.stream()
                .filter(email -> email != null && !email.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .forEach(this.adminEmails::add);
    }

    // Configurações de autenticação e recuperação de senha.
    public static class Security {

        private String tokenSecret = "";
        private long tokenExpirationMinutes = 480;
        private long resetTokenExpirationMinutes = 15;
        private boolean exposeResetToken = false;

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }

        public long getTokenExpirationMinutes() {
            return tokenExpirationMinutes;
        }

        public void setTokenExpirationMinutes(long tokenExpirationMinutes) {
            this.tokenExpirationMinutes = tokenExpirationMinutes;
        }

        public long getResetTokenExpirationMinutes() {
            return resetTokenExpirationMinutes;
        }

        public void setResetTokenExpirationMinutes(long resetTokenExpirationMinutes) {
            this.resetTokenExpirationMinutes = resetTokenExpirationMinutes;
        }

        public boolean isExposeResetToken() {
            return exposeResetToken;
        }

        public void setExposeResetToken(boolean exposeResetToken) {
            this.exposeResetToken = exposeResetToken;
        }
    }

    // Configurações de CORS.
    public static class Cors {

        private List<String> allowedOrigins = List.of();

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    // Configurações de acesso à API Cinemeta.
    public static class Cinemeta {

        private String metaBaseUrl = "https://v3-cinemeta.strem.io";
        private String catalogBaseUrl = "https://cinemeta-catalogs.strem.io";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;

        public String getMetaBaseUrl() {
            return metaBaseUrl;
        }

        public void setMetaBaseUrl(String metaBaseUrl) {
            this.metaBaseUrl = metaBaseUrl;
        }

        public String getCatalogBaseUrl() {
            return catalogBaseUrl;
        }

        public void setCatalogBaseUrl(String catalogBaseUrl) {
            this.catalogBaseUrl = catalogBaseUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}