package com.moviewatch.movie_watch_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Utilitários para obter os dados do usuário autenticado.
public final class SecurityUtils {

    private SecurityUtils() {
    }

    // Devolve o ID do usuário autenticado ou null quando não há autenticação.
    // O ID é colocado como principal pelo TokenAuthenticationFilter.
    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long userId) {
            return userId;
        }

        if (principal instanceof String userId) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    // Indica se existe um usuário autenticado na requisição atual.
    public static boolean isAuthenticated() {
        return currentUserId() != null;
    }
}