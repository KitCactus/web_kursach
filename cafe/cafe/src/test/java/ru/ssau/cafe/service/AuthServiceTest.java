package ru.ssau.cafe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ssau.cafe.dto.AuthResponseDto;
import ru.ssau.cafe.entity.Role;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для AuthService.
 *
 * Что такое юнит-тест:
 * - Тестирует ОДИН класс в изоляции (без базы данных, без сети).
 * - Зависимости (репозитории, кодировщик паролей) заменяются "заглушками" (Mockito @Mock).
 * - Работает быстро — запускается за миллисекунды.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("USER");

        testUser = new User("testuser", "encodedPassword123", "test@test.com", "Тест Тестов", "+79001234567");
        testUser.setRoles(List.of(role));
    }

    @Test
    @DisplayName("Успешная аутентификация — возвращает данные пользователя")
    void authenticate_validCredentials_returnsAuthResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true);

        AuthResponseDto result = authService.authenticate("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Authentication successful", result.getMessage());
    }

    @Test
    @DisplayName("Неверный пароль — возвращает null")
    void authenticate_wrongPassword_returnsNull() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123")).thenReturn(false);

        AuthResponseDto result = authService.authenticate("testuser", "wrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("Несуществующий пользователь — возвращает null")
    void authenticate_userNotFound_returnsNull() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        AuthResponseDto result = authService.authenticate("unknown", "anyPassword");

        assertNull(result);
        // Убеждаемся что passwordEncoder вообще не вызывался
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("userExists — пользователь есть в БД, возвращает true")
    void userExists_existingUser_returnsTrue() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertTrue(authService.userExists("testuser"));
    }

    @Test
    @DisplayName("userExists — пользователя нет, возвращает false")
    void userExists_nonExistingUser_returnsFalse() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertFalse(authService.userExists("ghost"));
    }
}
