package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.ActivityResponse;
import com.moviewatch.movie_watch_backend.entity.Activity;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.exception.UnauthorizedException;
import com.moviewatch.movie_watch_backend.repository.ActivityRepository;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import com.moviewatch.movie_watch_backend.security.AdminPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

// Service responsável pelo histórico de atividades.
//
// As atividades são registradas automaticamente quando o usuário
// adiciona/remove favoritos ou cria/edita/exclui avaliações.
// Também é possível registrar uma visualização de filme (MOVIE_VIEWED).
@Service
public class ActivityService {

    // Tipos de atividade aceitos pelo endpoint público.
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "FAVORITE_ADDED",
            "FAVORITE_REMOVED",
            "REVIEW_CREATED",
            "REVIEW_UPDATED",
            "REVIEW_DELETED",
            "MOVIE_VIEWED"
    );

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final AdminPolicy adminPolicy;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           AdminPolicy adminPolicy) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.adminPolicy = adminPolicy;
    }

    // Lista o histórico do usuário autenticado.
    @Transactional(readOnly = true)
    public List<ActivityResponse> listMine(Long userId) {
        return list(userId);
    }

    // Lista o histórico de um usuário.
    // Somente o próprio usuário ou um administrador pode consultar.
    @Transactional(readOnly = true)
    public List<ActivityResponse> listByUser(Long userId, Long requesterId) {
        requireSelfOrAdmin(userId, requesterId);
        return list(userId);
    }

    // Registra uma atividade no histórico.
    @Transactional
    public ActivityResponse record(Long userId, String activityType, String movieId) {
        if (activityType == null || !TIPOS_PERMITIDOS.contains(activityType)) {
            throw new BadRequestException("Tipo de atividade invalido.");
        }

        User user = userRepository.getReferenceById(userId);

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setActivityType(activityType);
        activity.setMovieId(normalizeMovieId(movieId));

        Activity saved = activityRepository.save(activity);
        return toResponse(saved);
    }

    // Lista as atividades de um usuário em ordem cronológica decrescente.
    private List<ActivityResponse> list(Long userId) {
        return activityRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Garante que o acesso seja do próprio usuário ou de um administrador.
    private void requireSelfOrAdmin(Long userId, Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedException("Nao autenticado.");
        }

        if (userId.equals(requesterId) || adminPolicy.isAdmin(requesterId)) {
            return;
        }

        throw new ForbiddenException("Acesso negado a dados de outro usuario.");
    }

    // Normaliza o identificador do filme; atividades podem não ter filme.
    private String normalizeMovieId(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            return null;
        }

        return movieId.trim();
    }

    // Converte a entidade em DTO.
    private ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getMovieId(),
                activity.getCreatedAt()
        );
    }
}