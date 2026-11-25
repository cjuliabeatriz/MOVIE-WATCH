package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.ExistsResponse;
import com.moviewatch.movie_watch_backend.dto.FavoriteRequest;
import com.moviewatch.movie_watch_backend.dto.FavoriteResponse;
import com.moviewatch.movie_watch_backend.entity.Favorite;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.NotFoundException;
import com.moviewatch.movie_watch_backend.repository.FavoriteRepository;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service responsável pelas regras de negócio dos favoritos.
//
// Regras principais:
// - Um filme não pode ser favoritado duas vezes pelo mesmo usuário.
// - Cada usuário só acessa os próprios favoritos.
// - O identificador do filme é o ID externo do Cinemeta.
// - Adicionar e remover geram atividades no histórico.
@Service
public class FavoriteService {

    private static final String FAVORITE_ADDED = "FAVORITE_ADDED";
    private static final String FAVORITE_REMOVED = "FAVORITE_REMOVED";

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           ActivityService activityService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    // Lista os favoritos do usuário, do mais recente para o mais antigo.
    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(Long userId) {
        return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Adiciona um filme aos favoritos do usuário.
    @Transactional
    public FavoriteResponse add(Long userId, FavoriteRequest request) {
        String movieId = normalizeMovieId(request.movieId());

        if (favoriteRepository.existsByUser_IdAndMovieId(userId, movieId)) {
            throw new ConflictException("Filme ja esta nos favoritos.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setMovieId(movieId);

        Favorite saved = favoriteRepository.save(favorite);
        activityService.record(userId, FAVORITE_ADDED, movieId);

        return toResponse(saved);
    }

    // Informa se um filme está favoritado pelo usuário.
    @Transactional(readOnly = true)
    public ExistsResponse exists(Long userId, String movieId) {
        String id = normalizeMovieId(movieId);
        boolean favorited = favoriteRepository.existsByUser_IdAndMovieId(userId, id);

        return new ExistsResponse(favorited);
    }

    // Remove um filme dos favoritos do usuário.
    @Transactional
    public void remove(Long userId, String movieId) {
        String id = normalizeMovieId(movieId);

        Favorite favorite = favoriteRepository.findByUser_IdAndMovieId(userId, id)
                .orElseThrow(() -> new NotFoundException("Favorito nao encontrado."));

        favoriteRepository.delete(favorite);
        activityService.record(userId, FAVORITE_REMOVED, id);
    }

    // Valida e normaliza o identificador externo do filme.
    private String normalizeMovieId(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new BadRequestException("O identificador do filme e obrigatorio.");
        }

        return movieId.trim();
    }

    // Converte a entidade em DTO.
    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getMovieId(),
                favorite.getCreatedAt()
        );
    }
}