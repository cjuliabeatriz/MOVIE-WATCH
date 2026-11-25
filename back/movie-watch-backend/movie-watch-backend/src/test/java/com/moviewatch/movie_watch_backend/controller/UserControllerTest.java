package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.UserResponse;
import com.moviewatch.movie_watch_backend.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes do controller protegido de usuários.
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    void getMeDeveRetornarPerfil() throws Exception {
        UserResponse resposta = new UserResponse(1L, "Ana", "ana@test.com", false, LocalDateTime.now());
        when(userService.getMe(1L)).thenReturn(resposta);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    void putMeDeveAtualizarPerfil() throws Exception {
        UserResponse resposta = new UserResponse(1L, "Ana Maria", "ana@test.com", false, LocalDateTime.now());
        when(userService.updateMe(eq(1L), any())).thenReturn(resposta);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana Maria\",\"email\":\"ana@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Maria"));

        verify(userService).updateMe(eq(1L), any());
    }

    @Test
    void putPasswordDeveAlterarSenha() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"antiga123\",\"newPassword\":\"nova1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso."));

        verify(userService).changePassword(eq(1L), any());
    }

    @Test
    void deleteMeDeveExcluirConta() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"senha123\"}"))
                .andExpect(status().isNoContent());

        verify(userService).deleteMe(eq(1L), any());
    }

    @Test
    void getTodosDeveListarUsuarios() throws Exception {
        when(userService.getAllUsers(1L)).thenReturn(List.of(
                new UserResponse(1L, "Ana", "ana@test.com", false, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void putStatusDeveAlterarBloqueio() throws Exception {
        when(userService.setBlocked(eq(2L), eq(true), eq(1L)))
                .thenReturn(new UserResponse(2L, "Bruno", "bruno@test.com", true, LocalDateTime.now()));

        mockMvc.perform(put("/api/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blocked\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true));
    }

    @Test
    void deleteUsuarioDeveExcluirPeloId() throws Exception {
        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq(2L), eq(1L));
    }
}