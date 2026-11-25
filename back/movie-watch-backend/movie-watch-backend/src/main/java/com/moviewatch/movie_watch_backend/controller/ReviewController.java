package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ReviewRequest;
import com.moviewatch.movie_watch_backend.dto.ReviewResponse;
import com.moviewatch.movie_watch_backend.dto.UpdateReviewRequest;
import com.moviewatch.movie_watch_backend.security.SecurityUtils;
import com.moviewatch.movie_watch_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller de avaliações e comentários.
// A listagem por filme é pública; as demais rotas são protegidas.
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Cria uma avaliação ou comentário.
    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Lista as avaliações de um filme.
    @GetMapping("/movie/{movieId}")
    public List<ReviewResponse> listByMovie(@PathVariable String movieId) {
        return reviewService.listByMovie(movieId);
    }

    // Lista as minhas avaliações.
    @GetMapping("/me")
    public List<ReviewResponse> listMine() {
        Long userId = SecurityUtils.currentUserId();
        return reviewService.listByUser(userId, userId);
    }

    // Lista as avaliações de um usuário (próprio caso ou administrador).
    @GetMapping("/user/{userId}")
    public List<ReviewResponse> listByUser(@PathVariable Long userId) {
        return reviewService.listByUser(userId, SecurityUtils.currentUserId());
    }

    // Consulta uma avaliação pelo ID.
    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable Long id) {
        return reviewService.getById(id, SecurityUtils.currentUserId());
    }

    // Edita uma avaliação.
    @PutMapping("/{id}")
    public ReviewResponse update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.update(id, SecurityUtils.currentUserId(), request);
    }

    // Exclui uma avaliação.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}