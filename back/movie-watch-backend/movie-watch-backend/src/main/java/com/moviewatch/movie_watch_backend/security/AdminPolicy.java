package com.moviewatch.movie_watch_backend.security;

import com.moviewatch.movie_watch_backend.config.MovieWatchProperties;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import org.springframework.stereotype.Component;

// Verifica se um usuário faz parte da lista de administradores.
//
// Como o banco não possui coluna de papel, a regra fica na
// propriedade moviewatch.admin.emails do application.properties.
@Component
public class AdminPolicy {

    private final UserRepository userRepository;
    private final MovieWatchProperties properties;

    public AdminPolicy(UserRepository userRepository, MovieWatchProperties properties) {
        this.userRepository = userRepository;
        this.properties = properties;
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> properties.getAdminEmails().contains(user.getEmail().toLowerCase()))
                .orElse(false);
    }
}