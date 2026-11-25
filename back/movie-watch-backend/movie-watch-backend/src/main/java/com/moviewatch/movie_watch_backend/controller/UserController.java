package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.BlockUserRequest;
import com.moviewatch.movie_watch_backend.dto.ChangePasswordRequest;
import com.moviewatch.movie_watch_backend.dto.DeleteAccountRequest;
import com.moviewatch.movie_watch_backend.dto.MessageResponse;
import com.moviewatch.movie_watch_backend.dto.UpdateUserRequest;
import com.moviewatch.movie_watch_backend.dto.UserResponse;
import com.moviewatch.movie_watch_backend.security.SecurityUtils;
import com.moviewatch.movie_watch_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller protegido de usuários.
// O usuário autenticado é obtido pelo SecurityUtils.
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Meu perfil.
    @GetMapping("/me")
    public UserResponse me() {
        return userService.getMe(SecurityUtils.currentUserId());
    }

    // Atualiza meu nome e e-mail.
    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest request) {
        return userService.updateMe(SecurityUtils.currentUserId(), request);
    }

    // Altera minha senha.
    @PutMapping("/me/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(SecurityUtils.currentUserId(), request);
        return new MessageResponse("Senha alterada com sucesso.");
    }

    // Exclui minha conta.
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@Valid @RequestBody DeleteAccountRequest request) {
        userService.deleteMe(SecurityUtils.currentUserId(), request);
        return ResponseEntity.noContent().build();
    }

    // Lista todos os usuários (somente administradores).
    @GetMapping
    public List<UserResponse> all() {
        return userService.getAllUsers(SecurityUtils.currentUserId());
    }

    // Consulta um usuário pelo ID (próprio caso ou administrador).
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getUser(id, SecurityUtils.currentUserId());
    }

    // Bloqueia ou desbloqueia uma conta (somente administradores).
    @PutMapping("/{id}/status")
    public UserResponse setStatus(@PathVariable Long id,
                                  @Valid @RequestBody BlockUserRequest request) {
        return userService.setBlocked(id, request.blocked(), SecurityUtils.currentUserId());
    }

    // Exclui uma conta (somente administradores).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.deleteUser(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}