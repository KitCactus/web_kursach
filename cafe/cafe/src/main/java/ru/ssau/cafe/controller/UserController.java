package ru.ssau.cafe.controller;

import ru.ssau.cafe.dto.UserDto;
import ru.ssau.cafe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @RequestBody UserDto userDto,
            @RequestParam(defaultValue = "USER") String role) {
        UserDto created = userService.createUser(userDto, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.changePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> updateActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        userService.updateActiveStatus(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Эндпоинты для текущего пользователя (доступны всем аутентифицированным) 

    @GetMapping("/me")
    public ResponseEntity<UserDto> getOwnProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getUserByUsername(auth.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateOwnProfile(Authentication auth, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateOwnProfile(auth.getName(), userDto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeOwnPassword(Authentication auth, @RequestParam String newPassword) {
        userService.changeOwnPassword(auth.getName(), newPassword);
        return ResponseEntity.ok().build();
    }
}