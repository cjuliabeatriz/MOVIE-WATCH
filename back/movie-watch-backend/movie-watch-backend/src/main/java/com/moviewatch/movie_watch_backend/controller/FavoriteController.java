package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ExistsResponse;
import com.moviewatch.movie_watch_backend.dto.FavoriteRequest;
import com.moviewatch.movie_watch_backend.dto.FavoriteResponse;
import com.moviewatch.movie_watch_backend.security.SecurityUtils;
import com.moviewatch.movie_watch_backend.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller protegido de favoritos.
// Todas as operações usam o usuário autenticado.
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Lista meus favoritos.
    @GetMapping
    public List<FavoriteResponse> list() {
        return favoriteService.list(SecurityUtils.currentUserId());
    }

    // Adiciona um filme aos meus favoritos.
    @PostMapping
    public ResponseEntity<FavoriteResponse> add(@Valid @RequestBody FavoriteRequest request) {
        FavoriteResponse response = favoriteService.add(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Informa se um filme está nos meus favoritos.
    @GetMapping("/{movieId}/exists")
    public ExistsResponse exists(@PathVariable String movieId) {
        return favoriteService.exists(SecurityUtils.currentUserId(), movieId);
    }

    // Remove um filme dos meus favoritos.
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(@PathVariable String movieId) {
        favoriteService.remove(SecurityUtils.currentUserId(), movieId);
        return ResponseEntity.noContent().build();
    }
}