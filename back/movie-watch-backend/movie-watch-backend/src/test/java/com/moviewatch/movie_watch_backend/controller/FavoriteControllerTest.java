package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ExistsResponse;
import com.moviewatch.movie_watch_backend.dto.FavoriteResponse;
import com.moviewatch.movie_watch_backend.service.FavoriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes do controller protegido de favoritos.
@WebMvcTest(controllers = FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @BeforeEach
    void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void postDeveRetornar201() throws Exception {
        when(favoriteService.add(eq(1L), any()))
                .thenReturn(new FavoriteResponse(10L, "tt1", LocalDateTime.now()));

        mockMvc.perform(post("/api/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":\"tt1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieId").value("tt1"));
    }

    @Test
    void getDeveListarFavoritos() throws Exception {
        when(favoriteService.list(1L)).thenReturn(List.of(
                new FavoriteResponse(10L, "tt1", LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getExistsDeveRetornarSituacao() throws Exception {
        when(favoriteService.exists(1L, "tt1")).thenReturn(new ExistsResponse(true));

        mockMvc.perform(get("/api/favorites/tt1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void deleteDeveRemoverFavorito() throws Exception {
        mockMvc.perform(delete("/api/favorites/tt1"))
                .andExpect(status().isNoContent());

        verify(favoriteService).remove(1L, "tt1");
    }
}