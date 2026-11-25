package com.moviewatch.movie_watch_backend.controller;

import com.moviewatch.movie_watch_backend.dto.ForgotPasswordResponse;
import com.moviewatch.movie_watch_backend.dto.LoginResponse;
import com.moviewatch.movie_watch_backend.dto.MessageResponse;
import com.moviewatch.movie_watch_backend.dto.UserResponse;
import com.moviewatch.movie_watch_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes do controller público de autenticação.
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerDeveRetornar201() throws Exception {
        UserResponse resposta = new UserResponse(1L, "Ana", "ana@test.com", false, LocalDateTime.now());
        when(userService.register(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@test.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    void registerDeveRetornar400QuandoDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalido\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginDeveRetornarToken() throws Exception {
        UserResponse usuario = new UserResponse(1L, "Ana", "ana@test.com", false, LocalDateTime.now());
        when(userService.login(any())).thenReturn(new LoginResponse("token-123", usuario));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"));
    }

    @Test
    void forgotPasswordDeveRetornar200() throws Exception {
        when(userService.forgotPassword(any()))
                .thenReturn(new ForgotPasswordResponse("Se o e-mail existir, um link sera enviado.", null));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetPasswordDeveRetornar200() throws Exception {
        when(userService.resetPassword(any()))
                .thenReturn(new MessageResponse("Senha redefinida com sucesso."));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token-123\",\"newPassword\":\"nova1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."));
    }
}