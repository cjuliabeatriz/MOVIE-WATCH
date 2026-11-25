package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.ActivityResponse;
import com.moviewatch.movie_watch_backend.entity.Activity;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.repository.ActivityRepository;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import com.moviewatch.movie_watch_backend.security.AdminPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitários das regras de negócio das atividades.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminPolicy adminPolicy;

    private ActivityService service;

    @BeforeEach
    void setUp() {
        service = new ActivityService(activityRepository, userRepository, adminPolicy);
    }

    @Test
    void recordDeveRecusarTipoInvalido() {
        assertThrows(BadRequestException.class,
                () -> service.record(1L, "QUALQUER_COISA", "tt1"));

        verify(activityRepository, never()).save(any());
    }

    @Test
    void recordDeveSalvarAtividadeValida() {
        User usuario = new User();
        usuario.setId(1L);
        usuario.setName("Ana");

        when(userRepository.getReferenceById(1L)).thenReturn(usuario);
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity atividade = invocation.getArgument(0);
            atividade.setId(50L);
            atividade.setCreatedAt(LocalDateTime.now());
            return atividade;
        });

        ActivityResponse resposta = service.record(1L, "MOVIE_VIEWED", "tt1");

        assertEquals("MOVIE_VIEWED", resposta.activityType());
        assertEquals("tt1", resposta.movieId());
    }

    @Test
    void listByUserDeveRejeitarConsultaDeOutroUsuario() {
        when(adminPolicy.isAdmin(1L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.listByUser(2L, 1L));
    }
}