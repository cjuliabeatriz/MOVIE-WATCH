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
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.exception.UnauthorizedException;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import com.moviewatch.movie_watch_backend.security.AdminPolicy;
import com.moviewatch.movie_watch_backend.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitários das regras de negócio de usuários.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private AdminPolicy adminPolicy;

    private MovieWatchProperties properties;
    private UserService service;

    // Usuário fictício usado em vários cenários.
    private User usuario;

    @BeforeEach
    void setUp() {
        properties = new MovieWatchProperties();
        properties.getSecurity().setTokenSecret("chave-de-teste-com-mais-de-32-caracteres");
        properties.getSecurity().setExposeResetToken(true);
        service = new UserService(userRepository, passwordEncoder, tokenService, adminPolicy, properties);

        usuario = new User();
        usuario.setId(1L);
        usuario.setName("Ana");
        usuario.setEmail("ana@test.com");
        usuario.setPasswordHash("hash");
        usuario.setIsBlocked(false);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerDeveCriarUsuarioComSenhaEmHash() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User salvo = invocation.getArgument(0);
            salvo.setId(1L);
            salvo.setCreatedAt(LocalDateTime.now());
            return salvo;
        });

        var resposta = service.register(new RegisterRequest("Ana", "ana@test.com", "senha123"));

        assertEquals(1L, resposta.id());
        assertEquals("ana@test.com", resposta.email());
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    void registerDeveRecusarEmailDuplicado() {
        when(userRepository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.register(new RegisterRequest("Ana", "ana@test.com", "senha123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginDeveDevolverTokenQuandoCredenciaisValidas() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
        when(tokenService.issue(1L)).thenReturn("token-123");

        LoginResponse resposta = service.login(new LoginRequest("ana@test.com", "senha123"));

        assertEquals("token-123", resposta.token());
        assertEquals("ana@test.com", resposta.user().email());
    }

    @Test
    void loginDeveRejeitarSenhaIncorreta() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("ana@test.com", "errada")));
    }

    @Test
    void loginDeveRejeitarContaBloqueada() {
        usuario.setIsBlocked(true);
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(ForbiddenException.class,
                () -> service.login(new LoginRequest("ana@test.com", "senha123")));
    }

    @Test
    void changePasswordDeveRejeitarSenhaAtualInvalida() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.changePassword(1L, new ChangePasswordRequest("errada", "nova1234")));
    }

    @Test
    void changePasswordDeveAtualizarHash() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("antiga", "hash")).thenReturn(true);
        when(passwordEncoder.matches("nova1234", "hash")).thenReturn(false);
        when(passwordEncoder.encode("nova1234")).thenReturn("novo-hash");

        service.changePassword(1L, new ChangePasswordRequest("antiga", "nova1234"));

        assertEquals("novo-hash", usuario.getPasswordHash());
        verify(userRepository).save(usuario);
    }

    @Test
    void forgotPasswordDeveGerarTokenParaEmailExistente() {
        when(userRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordResponse resposta = service.forgotPassword(new ForgotPasswordRequest("ana@test.com"));

        assertNotNull(resposta.token());
        assertNotNull(usuario.getResetTokenHash());
        assertNotNull(usuario.getResetTokenExpires());
    }

    @Test
    void forgotPasswordNaoDeveRevelarEmailInexistente() {
        when(userRepository.findByEmail("ninguem@test.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse resposta = service.forgotPassword(new ForgotPasswordRequest("ninguem@test.com"));

        assertNull(resposta.token());
        assertEquals("Se o e-mail existir, um link de recuperacao sera enviado.", resposta.message());
    }

    @Test
    void resetPasswordDeveRejeitarTokenInvalido() {
        when(userRepository.findByResetTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> service.resetPassword(new ResetPasswordRequest("token-invalido", "nova1234")));
    }

    @Test
    void resetPasswordDeveRejeitarTokenExpirado() {
        usuario.setResetTokenHash("hash-token");
        usuario.setResetTokenExpires(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByResetTokenHash(anyString())).thenReturn(Optional.of(usuario));

        assertThrows(BadRequestException.class,
                () -> service.resetPassword(new ResetPasswordRequest("token-qualquer", "nova1234")));

        assertNull(usuario.getResetTokenHash());
        assertNull(usuario.getResetTokenExpires());
    }

    @Test
    void resetPasswordDeveTrocarSenhaELimparToken() {
        usuario.setResetTokenHash("hash-token");
        usuario.setResetTokenExpires(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByResetTokenHash(anyString())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nova1234")).thenReturn("novo-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse resposta = service.resetPassword(new ResetPasswordRequest("token-valido", "nova1234"));

        assertEquals("Senha redefinida com sucesso.", resposta.message());
        assertEquals("novo-hash", usuario.getPasswordHash());
        assertNull(usuario.getResetTokenHash());
        assertNull(usuario.getResetTokenExpires());
    }

    @Test
    void deleteMeDeveRejeitarSenhaInvalida() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.deleteMe(1L, new DeleteAccountRequest("errada")));
    }

    @Test
    void setBlockedDeveRejeitarUsuarioNaoAdministrador() {
        when(adminPolicy.isAdmin(1L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.setBlocked(2L, true, 1L));
    }

    @Test
    void setBlockedDeveImpedirAutoBloqueio() {
        when(adminPolicy.isAdmin(1L)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.setBlocked(1L, true, 1L));
    }
}