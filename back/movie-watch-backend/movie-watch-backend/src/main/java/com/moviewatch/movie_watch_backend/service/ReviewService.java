package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.ReviewRequest;
import com.moviewatch.movie_watch_backend.dto.ReviewResponse;
import com.moviewatch.movie_watch_backend.dto.UpdateReviewRequest;
import com.moviewatch.movie_watch_backend.entity.Review;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.exception.NotFoundException;
import com.moviewatch.movie_watch_backend.exception.UnauthorizedException;
import com.moviewatch.movie_watch_backend.repository.ReviewRepository;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import com.moviewatch.movie_watch_backend.security.AdminPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service responsável pelas regras de negócio das avaliações e comentários.
//
// Regras principais:
// - Nota entre 1 e 5, quando informada.
// - Comentário sem nota é permitido.
// - É obrigatório informar nota ou comentário.
// - Um usuário só pode avaliar um filme uma vez.
// - Apenas o autor pode editar ou excluir a própria avaliação.
@Service
public class ReviewService {

    private static final String REVIEW_CREATED = "REVIEW_CREATED";
    private static final String REVIEW_UPDATED = "REVIEW_UPDATED";
    private static final String REVIEW_DELETED = "REVIEW_DELETED";

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final AdminPolicy adminPolicy;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ActivityService activityService,
                         AdminPolicy adminPolicy) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.adminPolicy = adminPolicy;
    }

    // Lista as avaliações de um filme. Endpoint público.
    @Transactional(readOnly = true)
    public List<ReviewResponse> listByMovie(String movieId) {
        String id = normalizeMovieId(movieId);

        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Lista as avaliações de um usuário.
    // Somente o próprio usuário ou um administrador pode consultar.
    @Transactional(readOnly = true)
    public List<ReviewResponse> listByUser(Long userId, Long requesterId) {
        requireSelfOrAdmin(userId, requesterId);

        return reviewRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Consulta uma avaliação pelo ID.
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id, Long requesterId) {
        Review review = findReview(id);
        requireOwnerOrAdmin(review, requesterId);

        return toResponse(review);
    }

    // Cria uma avaliação ou comentário para um filme.
    @Transactional
    public ReviewResponse create(Long userId, ReviewRequest request) {
        String movieId = normalizeMovieId(request.movieId());
        validateContent(request.rating(), request.comment());

        if (reviewRepository.existsByUser_IdAndMovieId(userId, movieId)) {
            throw new ConflictException("Voce ja avaliou este filme.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));

        Review review = new Review();
        review.setUser(user);
        review.setMovieId(movieId);
        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));

        Review saved = reviewRepository.save(review);
        activityService.record(userId, REVIEW_CREATED, movieId);

        return toResponse(saved);
    }

    // Edita a nota e/ou o comentário de uma avaliação existente.
    @Transactional
    public ReviewResponse update(Long id, Long requesterId, UpdateReviewRequest request) {
        Review review = findReview(id);

        if (!review.getUser().getId().equals(requesterId)) {
            throw new ForbiddenException("Voce so pode editar suas proprias avaliacoes.");
        }

        validateContent(request.rating(), request.comment());

        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));

        Review saved = reviewRepository.save(review);
        activityService.record(review.getUser().getId(), REVIEW_UPDATED, review.getMovieId());

        return toResponse(saved);
    }

    // Exclui uma avaliação. Apenas o autor pode excluir.
    @Transactional
    public void delete(Long id, Long requesterId) {
        Review review = findReview(id);

        if (!review.getUser().getId().equals(requesterId)) {
            throw new ForbiddenException("Voce so pode excluir suas proprias avaliacoes.");
        }

        Long userId = review.getUser().getId();
        String movieId = review.getMovieId();

        reviewRepository.delete(review);
        activityService.record(userId, REVIEW_DELETED, movieId);
    }

    // Busca uma avaliação ou lança 404.
    private Review findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Avaliacao nao encontrada."));
    }

    // Valida a regra de conteúdo: nota ou comentário.
    private void validateContent(Short rating, String comment) {
        if (rating == null && (comment == null || comment.isBlank())) {
            throw new BadRequestException("Informe a nota ou o comentario.");
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BadRequestException("A nota deve estar entre 1 e 5.");
        }
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

    // Garante que apenas o autor ou um administrador consulte a avaliação.
    private void requireOwnerOrAdmin(Review review, Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedException("Nao autenticado.");
        }

        if (review.getUser().getId().equals(requesterId) || adminPolicy.isAdmin(requesterId)) {
            return;
        }

        throw new ForbiddenException("Acesso negado a dados de outro usuario.");
    }

    // Valida e normaliza o identificador externo do filme.
    private String normalizeMovieId(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            throw new BadRequestException("O identificador do filme e obrigatorio.");
        }

        return movieId.trim();
    }

    // Remove espaços e transforma comentário vazio em null.
    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Converte a entidade em DTO, incluindo o nome do autor.
    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getMovieId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}