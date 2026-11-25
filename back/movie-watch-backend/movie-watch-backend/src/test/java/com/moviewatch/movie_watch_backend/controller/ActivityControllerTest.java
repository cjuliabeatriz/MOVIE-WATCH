package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ActivityResponse;
import com.moviewatch.movie_watch_backend.service.ActivityService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes do controller de atividades.
@WebMvcTest(controllers = ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

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
    void getMeDeveListarHistorico() throws Exception {
        when(activityService.listMine(1L)).thenReturn(List.of(
                new ActivityResponse(50L, "MOVIE_VIEWED", "tt1", LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/activities/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void postDeveRegistrarAtividade() throws Exception {
        when(activityService.record(eq(1L), eq("MOVIE_VIEWED"), eq("tt1")))
                .thenReturn(new ActivityResponse(50L, "MOVIE_VIEWED", "tt1", LocalDateTime.now()));

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activityType\":\"MOVIE_VIEWED\",\"movieId\":\"tt1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityType").value("MOVIE_VIEWED"));
    }
}