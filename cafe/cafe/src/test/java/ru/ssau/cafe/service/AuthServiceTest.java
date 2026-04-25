package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.AuthResponseDto;
import ru.ssau.cafe.entity.Role;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    @Test
    void authenticate_validCredentials_returnsResponse() {
        User user = new User("ivan", passwordEncoder.encode("pass123"), "ivan@mail.ru", "Иван", "79001234567");
        Role role = new Role("ADMIN");
        user.setRoles(List.of(role));

        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));

        AuthResponseDto result = authService.authenticate("ivan", "pass123");

        assertNotNull(result);
        assertEquals("ivan", result.getUsername());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void authenticate_wrongPassword_returnsNull() {
        User user = new User("ivan", passwordEncoder.encode("pass123"), "ivan@mail.ru", "Иван", "79001234567");
        user.setRoles(List.of(new Role("USER")));

        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));

        AuthResponseDto result = authService.authenticate("ivan", "wrongpassword");

        assertNull(result);
    }

    @Test
    void authenticate_userNotFound_returnsNull() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        AuthResponseDto result = authService.authenticate("nobody", "pass123");

        assertNull(result);
    }

    @Test
    void authenticate_savesLastLoginAt() {
        User user = new User("ivan", passwordEncoder.encode("pass123"), "ivan@mail.ru", "Иван", "79001234567");
        user.setRoles(List.of(new Role("USER")));

        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.authenticate("ivan", "pass123");

        verify(userRepository).save(user);
        assertNotNull(user.getLastLoginAt());
    }
}
