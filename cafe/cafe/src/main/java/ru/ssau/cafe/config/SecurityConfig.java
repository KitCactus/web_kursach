package ru.ssau.cafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ========== ПУБЛИЧНЫЕ ЭНДПОИНТЫ (не требуют авторизации) ==========
                        // Для бота (доступно всем)
                        .requestMatchers("/menu/bot").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // Для Telegram бота (вебхуки)
                        .requestMatchers("/webhook/**").permitAll()

                        // ========== ЭНДПОИНТЫ ДЛЯ СОТРУДНИКОВ И АДМИНОВ ==========
                        // Меню (просмотр, редактирование, добавление)
                        .requestMatchers(HttpMethod.GET, "/menu").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/menu/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.POST, "/menu").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/menu/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/menu/*/availability").hasAnyRole("ADMIN", "STAFF")
                        // Заказы (просмотр, изменение статуса)
                        .requestMatchers(HttpMethod.GET, "/orders/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/status").hasAnyRole("ADMIN", "STAFF")

                        // ========== ПРОФИЛЬ (доступен любому авторизованному) ==========
                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/users/me/password").authenticated()

                        // ========== ЭНДПОИНТЫ ТОЛЬКО ДЛЯ АДМИНОВ ==========
                        // Управление пользователями
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                        // Скрытие товаров
                        .requestMatchers(HttpMethod.PATCH, "/menu/*/visibility").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/menu/*").hasRole("ADMIN")
                        // Отчёты
                        .requestMatchers("/reports/**").hasRole("ADMIN")

                        // Всё остальное — требует аутентификации
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                        })
                );

        return http.build();
    }
}