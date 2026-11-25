package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ForgotPasswordRequest;
import com.moviewatch.movie_watch_backend.dto.ForgotPasswordResponse;
import com.moviewatch.movie_watch_backend.dto.LoginRequest;
import com.moviewatch.movie_watch_backend.dto.LoginResponse;
import com.moviewatch.movie_watch_backend.dto.MessageResponse;
import com.moviewatch.movie_watch_backend.dto.RegisterRequest;
import com.moviewatch.movie_watch_backend.dto.ResetPasswordRequest;
import com.moviewatch.movie_watch_backend.dto.UserResponse;
import com.moviewatch.movie_watch_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller público de autenticação.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Cadastra um novo usuário.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Autentica o usuário e devolve o token Bearer.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // Solicita a recuperação de senha.
    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    // Conclui a recuperação de senha.
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }
}