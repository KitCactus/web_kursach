package ru.ssau.cafe.controller;

import ru.ssau.cafe.dto.AuthRequestDto;
import ru.ssau.cafe.dto.AuthResponseDto;
import ru.ssau.cafe.dto.UserDto;
import ru.ssau.cafe.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestParam String username) {
        UserDto user = authService.getCurrentUser(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
        AuthResponseDto response = authService.authenticate(request.getUsername(), request.getPassword());

        if (response == null) {
            return ResponseEntity.status(401).body(new AuthResponseDto(
                    null, null, null, null, "Invalid username or password"
            ));
        }

        return ResponseEntity.ok(response);
    }
}