package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.UserDto;
import ru.ssau.cafe.entity.Role;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.exception.ResourceNotFoundException;
import ru.ssau.cafe.repository.RoleRepository;
import ru.ssau.cafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto convertToDto(User user) {
        String roleName = user.getRoles().isEmpty() ? "USER" : user.getRoles().get(0).getName();
        UserDto dto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                roleName
        );
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        return dto;
    }

    private List<UserDto> convertToDtoList(List<User> users) {
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<UserDto> getAllUsers() {
        return convertToDtoList(userRepository.findAll());
    }

    public List<UserDto> getAllActiveUsers() {
        return convertToDtoList(userRepository.findAllActive());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto, String roleName) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userDto.getPhone() == null || userDto.getPhone().isBlank()) {
            throw new RuntimeException("Phone is required");
        }
        if (!userDto.getPhone().matches("\\d+")) {
            throw new RuntimeException("Phone must contain only digits");
        }

        String rawPassword = (userDto.getPassword() != null && !userDto.getPassword().isBlank())
                ? userDto.getPassword() : "default123";
        User user = new User(
                userDto.getUsername(),
                passwordEncoder.encode(rawPassword),
                userDto.getEmail() != null && !userDto.getEmail().isBlank() ? userDto.getEmail() : null,
                userDto.getFullName() != null ? userDto.getFullName() : userDto.getUsername(),
                userDto.getPhone()
        );

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRoles(Collections.singletonList(role));

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userDto.getFullName() != null) user.setFullName(userDto.getFullName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getPhone() != null) {
            if (!userDto.getPhone().matches("\\d+")) {
                throw new RuntimeException("Phone must contain only digits");
            }
            user.setPhone(userDto.getPhone());
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Transactional
    public void updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(Collections.singletonList(role));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        // Очищаем связи перед удалением, иначе PostgreSQL упадёт на FK
        user.getRoles().clear();
        userRepository.save(user);
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Пользователь обновляет свой собственный профиль (имя, email, телефон).
     * Логин изменить нельзя.
     */
    @Transactional
    public UserDto updateOwnProfile(String username, UserDto userDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (userDto.getFullName() != null) user.setFullName(userDto.getFullName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getPhone() != null) user.setPhone(userDto.getPhone());
        return convertToDto(userRepository.save(user));
    }

    @Transactional
    public void updateActiveStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(active);
        userRepository.save(user);
    }

    /**
     * Пользователь меняет свой собственный пароль.
     */
    @Transactional
    public void changeOwnPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}