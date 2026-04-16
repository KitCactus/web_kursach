package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.AuthResponseDto;
import ru.ssau.cafe.dto.UserDto;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto convertToDto(User user) {
        String roleName = user.getRoles().isEmpty() ? "USER" : user.getRoles().get(0).getName();
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                roleName
        );
    }

    /**
     * Аутентификация пользователя по username и password
     * @param username логин
     * @param rawPassword пароль в открытом виде
     * @return AuthResponseDto с информацией о пользователе или null
     */
    @Transactional
    public AuthResponseDto authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return null;
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }

        // Записываем время последнего входа
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String roleName = user.getRoles().isEmpty() ? "USER" : user.getRoles().get(0).getName();

        return new AuthResponseDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                roleName,
                "Authentication successful"
        );
    }

    /**
     * Получение текущего пользователя по username (используется после Basic Auth)
     * @param username логин
     * @return UserDto с информацией о пользователе
     */
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return convertToDto(user);
    }

    /**
     * Проверка существования пользователя по username
     * @param username логин
     * @return true если существует, false если нет
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}