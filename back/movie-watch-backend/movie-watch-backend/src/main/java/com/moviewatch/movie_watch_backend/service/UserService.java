package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.config.MovieWatchProperties;
import com.moviewatch.movie_watch_backend.dto.ChangePasswordRequest;
import com.moviewatch.movie_watch_backend.dto.DeleteAccountRequest;
import com.moviewatch.movie_watch_backend.dto.ForgotPasswordRequest;
import com.moviewatch.movie_watch_backend.dto.ForgotPasswordResponse;
import com.moviewatch.movie_watch_backend.dto.LoginRequest;
import com.moviewatch.movie_watch_backend.dto.LoginResponse;
import com.moviewatch.movie_watch_backend.dto.MessageResponse;
import com.moviewatch.movie_watch_backend.dto.RegisterRequest;
import com.moviewatch.movie_watch_backend.dto.ResetPasswordRequest;
import com.moviewatch.movie_watch_backend.dto.UpdateUserRequest;
import com.moviewatch.movie_watch_backend.dto.UserResponse;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.exception.NotFoundException;
import com.moviewatch.movie_watch_backend.exception.UnauthorizedException;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import com.moviewatch.movie_watch_backend.security.AdminPolicy;
import com.moviewatch.movie_watch_backend.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

// Service responsável pelas regras de negócio dos usuários.
//
// Regras principais:
// - E-mail duplicado é recusado.
// - Senhas são armazenadas apenas como hash BCrypt.
// - Contas bloqueadas não podem fazer login.
// - Um usuário só acessa os próprios dados, salvo administradores.
// - Tokens de recuperação são armazenados como hash SHA-256 e expiram.
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AdminPolicy adminPolicy;
    private final MovieWatchProperties properties;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       AdminPolicy adminPolicy,
                       MovieWatchProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.adminPolicy = adminPolicy;
        this.properties = properties;
    }

    // Cadastra um novo usuário.
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("E-mail ja cadastrado.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsBlocked(false);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    // Autentica o usuário e devolve o token Bearer.
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas."));

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new ForbiddenException("Conta bloqueada.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais invalidas.");
        }

        String token = tokenService.issue(user.getId());
        return new LoginResponse(token, toResponse(user));
    }

    // Consulta o perfil do usuário autenticado.
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return toResponse(findUser(userId));
    }

    // Consulta um usuário pelo ID.
    // Somente o próprio usuário ou um administrador pode consultar.
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id, Long requesterId) {
        requireSelfOrAdmin(id, requesterId);
        return toResponse(findUser(id));
    }

    // Lista todos os usuários. Acesso restrito a administradores.
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Long requesterId) {
        requireAdmin(requesterId);

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Atualiza o próprio perfil.
    @Transactional
    public UserResponse updateMe(Long userId, UpdateUserRequest request) {
        return updateUserInternal(userId, request);
    }

    // Atualiza um usuário pelo ID (próprio caso ou administrador).
    @Transactional
    public UserResponse updateUser(Long id, Long requesterId, UpdateUserRequest request) {
        requireSelfOrAdmin(id, requesterId);
        return updateUserInternal(id, request);
    }

    // Altera a senha do usuário autenticado.
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Senha atual invalida.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("A nova senha deve ser diferente da atual.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // Exclui a própria conta, exigindo a senha atual.
    @Transactional
    public void deleteMe(Long userId, DeleteAccountRequest request) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Senha invalida.");
        }

        userRepository.delete(user);
    }

    // Exclui uma conta. Acesso restrito a administradores.
    @Transactional
    public void deleteUser(Long id, Long requesterId) {
        requireAdmin(requesterId);

        if (id.equals(requesterId)) {
            throw new BadRequestException("Use a exclusao da propria conta para se remover.");
        }

        User user = findUser(id);
        userRepository.delete(user);
    }

    // Bloqueia ou desbloqueia uma conta. Acesso restrito a administradores.
    @Transactional
    public UserResponse setBlocked(Long id, boolean blocked, Long requesterId) {
        requireAdmin(requesterId);

        if (id.equals(requesterId)) {
            throw new BadRequestException("Voce nao pode bloquear a propria conta.");
        }

        User user = findUser(id);
        user.setIsBlocked(blocked);
        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    // Solicita a recuperação de senha.
    // A resposta é sempre a mesma para não revelar se o e-mail existe.
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        Optional<User> usuario = userRepository.findByEmail(email);

        if (usuario.isEmpty() || Boolean.TRUE.equals(usuario.get().getIsBlocked())) {
            return new ForgotPasswordResponse(
                    "Se o e-mail existir, um link de recuperacao sera enviado.",
                    null
            );
        }

        User user = usuario.get();
        String rawToken = gerarToken();

        user.setResetTokenHash(hashToken(rawToken));
        user.setResetTokenExpires(
                LocalDateTime.now().plusMinutes(properties.getSecurity().getResetTokenExpirationMinutes())
        );

        userRepository.save(user);

        // Em desenvolvimento, a propriedade permite devolver o token para teste.
        String tokenParaTeste = properties.getSecurity().isExposeResetToken() ? rawToken : null;

        return new ForgotPasswordResponse(
                "Se o e-mail existir, um link de recuperacao sera enviado.",
                tokenParaTeste
        );
    }

    // Conclui a recuperação de senha validando o token e sua expiração.
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String hash = hashToken(request.token());

        User user = userRepository.findByResetTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Token invalido."));

        if (user.getResetTokenExpires() == null
                || LocalDateTime.now().isAfter(user.getResetTokenExpires())) {

            user.setResetTokenHash(null);
            user.setResetTokenExpires(null);
            userRepository.save(user);

            throw new BadRequestException("Token expirado.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetTokenHash(null);
        user.setResetTokenExpires(null);
        userRepository.save(user);

        return new MessageResponse("Senha redefinida com sucesso.");
    }

    // Atualiza nome e e-mail, verificando duplicidade.
    private UserResponse updateUserInternal(Long id, UpdateUserRequest request) {
        User user = findUser(id);
        String email = normalizeEmail(request.email());

        userRepository.findByEmail(email)
                .filter(outro -> !id.equals(outro.getId()))
                .ifPresent(outro -> {
                    throw new ConflictException("E-mail ja cadastrado.");
                });

        user.setName(request.name().trim());
        user.setEmail(email);
        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    // Busca um usuário ou lança 404.
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
    }

    // Garante que o acesso seja do próprio usuário ou de um administrador.
    private void requireSelfOrAdmin(Long id, Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedException("Nao autenticado.");
        }

        if (id.equals(requesterId) || adminPolicy.isAdmin(requesterId)) {
            return;
        }

        throw new ForbiddenException("Acesso negado a dados de outro usuario.");
    }

    // Garante que o solicitante seja administrador.
    private void requireAdmin(Long requesterId) {
        if (requesterId == null) {
            throw new UnauthorizedException("Nao autenticado.");
        }

        if (!adminPolicy.isAdmin(requesterId)) {
            throw new ForbiddenException("Acesso restrito a administradores.");
        }
    }

    // Normaliza o e-mail para o formato usado no banco.
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    // Gera um token aleatório de recuperação.
    private String gerarToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Aplica SHA-256 no token antes de armazená-lo no banco.
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Nao foi possivel gerar o hash do token.", ex);
        }
    }

    // Converte a entidade em DTO, sem expor dados sensíveis.
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getIsBlocked(),
                user.getCreatedAt()
        );
    }
}