package com.moviewatch.movie_watch_backend.service;

import com.moviewatch.movie_watch_backend.dto.FavoriteRequest;
import com.moviewatch.movie_watch_backend.dto.FavoriteResponse;
import com.moviewatch.movie_watch_backend.entity.Favorite;
import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.exception.ConflictException;
import com.moviewatch.movie_watch_backend.exception.NotFoundException;
import com.moviewatch.movie_watch_backend.repository.FavoriteRepository;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitários das regras de negócio dos favoritos.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityService activityService;

    private FavoriteService service;
    private User usuario;

    @BeforeEach
    void setUp() {
        service = new FavoriteService(favoriteRepository, userRepository, activityService);

        usuario = new User();
        usuario.setId(1L);
        usuario.setName("Ana");
        usuario.setEmail("ana@test.com");
    }

    @Test
    void addDeveCriarFavoritoERegistrarAtividade() {
        when(favoriteRepository.existsByUser_IdAndMovieId(1L, "tt1")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> {
            Favorite favorito = invocation.getArgument(0);
            favorito.setId(10L);
            favorito.setCreatedAt(LocalDateTime.now());
            return favorito;
        });

        FavoriteResponse resposta = service.add(1L, new FavoriteRequest("tt1"));

        assertEquals(10L, resposta.id());
        assertEquals("tt1", resposta.movieId());
        verify(activityService).record(1L, "FAVORITE_ADDED", "tt1");
    }

    @Test
    void addDeveRecusarFavoritoDuplicado() {
        when(favoriteRepository.existsByUser_IdAndMovieId(1L, "tt1")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.add(1L, new FavoriteRequest("tt1")));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void removeDeveLancarNotFoundQuandoFavoritoNaoExiste() {
        when(favoriteRepository.findByUser_IdAndMovieId(1L, "tt1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.remove(1L, "tt1"));

        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void listDeveRetornarFavoritosDoUsuario() {
        Favorite favorito = new Favorite();
        favorito.setId(10L);
        favorito.setMovieId("tt1");
        favorito.setCreatedAt(LocalDateTime.now());

        when(favoriteRepository.findByUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(favorito));

        List<FavoriteResponse> resposta = service.list(1L);

        assertEquals(1, resposta.size());
        assertEquals("tt1", resposta.get(0).movieId());
    }
}