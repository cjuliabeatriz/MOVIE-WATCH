package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ReviewResponse;
import com.moviewatch.movie_watch_backend.service.ReviewService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes do controller de avaliações.
@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

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

    // Cria uma resposta fictícia de avaliação.
    private ReviewResponse avaliacao() {
        LocalDateTime agora = LocalDateTime.now();
        return new ReviewResponse(100L, 1L, "Ana", "tt1", (short) 5, "Muito bom", agora, agora);
    }

    @Test
    void postDeveRetornar201() throws Exception {
        when(reviewService.create(eq(1L), any())).thenReturn(avaliacao());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":\"tt1\",\"rating\":5,\"comment\":\"Muito bom\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieId").value("tt1"));
    }

    @Test
    void getPorFilmeDeveListarAvaliacoes() throws Exception {
        when(reviewService.listByMovie("tt1")).thenReturn(List.of(avaliacao()));

        mockMvc.perform(get("/api/reviews/movie/tt1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void putDeveEditarAvaliacao() throws Exception {
        when(reviewService.update(eq(100L), eq(1L), any())).thenReturn(avaliacao());

        mockMvc.perform(put("/api/reviews/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Mudei de ideia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));

        verify(reviewService).update(eq(100L), eq(1L), any());
    }

    @Test
    void deleteDeveExcluirAvaliacao() throws Exception {
        mockMvc.perform(delete("/api/reviews/100"))
                .andExpect(status().isNoContent());

        verify(reviewService).delete(100L, 1L);
    }
}