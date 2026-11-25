package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.ReviewRequest;
import com.moviewatch.movie_watch_backend.dto.ReviewResponse;
import com.moviewatch.movie_watch_backend.dto.UpdateReviewRequest;
import com.moviewatch.movie_watch_backend.entity.Review;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.BadRequestException;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.ForbiddenException;
import com.moviewatch.movie_watch_backend.repository.ReviewRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitários das regras de negócio das avaliações.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityService activityService;

    @Mock
    private AdminPolicy adminPolicy;

    private ReviewService service;
    private User usuario;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, userRepository, activityService, adminPolicy);

        usuario = new User();
        usuario.setId(1L);
        usuario.setName("Ana");
        usuario.setEmail("ana@test.com");
    }

    @Test
    void createDeveCriarAvaliacaoERegistrarAtividade() {
        when(reviewRepository.existsByUser_IdAndMovieId(1L, "tt1")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review avaliacao = invocation.getArgument(0);
            avaliacao.setId(100L);
            avaliacao.setCreatedAt(LocalDateTime.now());
            avaliacao.setUpdatedAt(LocalDateTime.now());
            return avaliacao;
        });

        ReviewResponse resposta = service.create(
                1L,
                new ReviewRequest("tt1", (short) 5, "Muito bom")
        );

        assertEquals((short) 5, resposta.rating());
        assertEquals("Muito bom", resposta.comment());
        verify(activityService).record(1L, "REVIEW_CREATED", "tt1");
    }

    @Test
    void createDeveRecusarAvaliacaoSemNotaESemComentario() {
        assertThrows(BadRequestException.class,
                () -> service.create(1L, new ReviewRequest("tt1", null, "   ")));
    }

    @Test
    void createDeveRecusarAvaliacaoDuplicada() {
        when(reviewRepository.existsByUser_IdAndMovieId(1L, "tt1")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.create(1L, new ReviewRequest("tt1", (short) 4, null)));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateDeveRejeitarAvaliacaoDeOutroUsuario() {
        Review avaliacao = avaliacaoDoUsuario(2L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        assertThrows(ForbiddenException.class,
                () -> service.update(1L, 1L, new UpdateReviewRequest((short) 3, "Alterado")));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteDeveRejeitarAvaliacaoDeOutroUsuario() {
        Review avaliacao = avaliacaoDoUsuario(2L);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        assertThrows(ForbiddenException.class,
                () -> service.delete(1L, 1L));

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void listByUserDeveRejeitarConsultaDeOutroUsuario() {
        when(adminPolicy.isAdmin(1L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.listByUser(2L, 1L));
    }

    // Cria uma avaliação fictícia para um usuário com o ID informado.
    private Review avaliacaoDoUsuario(Long userId) {
        User autor = new User();
        autor.setId(userId);
        autor.setName("Autor");

        Review avaliacao = new Review();
        avaliacao.setId(1L);
        avaliacao.setUser(autor);
        avaliacao.setMovieId("tt1");
        avaliacao.setRating((short) 4);
        return avaliacao;
    }
}