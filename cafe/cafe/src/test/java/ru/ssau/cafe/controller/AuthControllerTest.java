package ru.ssau.cafe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ssau.cafe.dto.AuthRequestDto;
import ru.ssau.cafe.dto.AuthResponseDto;
import ru.ssau.cafe.security.CustomUserDetailsService;
import ru.ssau.cafe.service.AuthService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционный тест для AuthController.
 *
 * Что такое интеграционный тест:
 * - Тестирует несколько слоёв вместе (контроллер + безопасность + HTTP).
 * - @WebMvcTest поднимает только веб-слой (без базы данных).
 * - MockMvc позволяет "делать запросы" к контроллеру без реального HTTP-сервера.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /auth/login — правильные данные возвращают 200 и данные пользователя")
    void login_validCredentials_returns200() throws Exception {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("admin");
        request.setPassword("admin123");

        AuthResponseDto response = new AuthResponseDto(1L, "admin", "Администратор", "ADMIN", "Authentication successful");
        when(authService.authenticate("admin", "admin123")).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login — неверные данные возвращают 401")
    void login_invalidCredentials_returns401() throws Exception {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("admin");
        request.setPassword("wrongPassword");

        when(authService.authenticate("admin", "wrongPassword")).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
