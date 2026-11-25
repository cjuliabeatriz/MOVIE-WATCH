package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ActivityRequest;
import com.moviewatch.movie_watch_backend.dto.ActivityResponse;
import com.moviewatch.movie_watch_backend.security.SecurityUtils;
import com.moviewatch.movie_watch_backend.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller protegido do histórico de atividades.
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // Lista o meu histórico.
    @GetMapping("/me")
    public List<ActivityResponse> listMine() {
        return activityService.listMine(SecurityUtils.currentUserId());
    }

    // Lista o histórico de um usuário (próprio caso ou administrador).
    @GetMapping("/user/{userId}")
    public List<ActivityResponse> listByUser(@PathVariable Long userId) {
        return activityService.listByUser(userId, SecurityUtils.currentUserId());
    }

    // Registra uma interação manual (por exemplo, MOVIE_VIEWED).
    @PostMapping
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.record(
                SecurityUtils.currentUserId(),
                request.activityType(),
                request.movieId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}