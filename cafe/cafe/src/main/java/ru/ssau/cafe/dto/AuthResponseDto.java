package ru.ssau.cafe.dto;

public class AuthResponseDto {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String message;

    public AuthResponseDto() {}

    public AuthResponseDto(Long id, String username, String fullName, String role, String message) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}