package ru.ssau.cafe.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

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
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
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
                        // SPA (главная страница и статические файлы)
                        .requestMatchers("/", "/index.html").permitAll()
                        .requestMatchers("/assets/**", "/scripts/**", "/styles/**").permitAll()
                        .requestMatchers("/*.js", "/*.css", "/*.ico", "/*.svg", "/*.png", "/*.jpg", "/*.gif").permitAll()
                        // Angular SPA маршруты - перенаправляются на index.html
                        .requestMatchers("/dashboard", "/menu", "/orders", "/admin/**", "/login").permitAll()

                        // Для бота (доступно всем)
                        .requestMatchers("/api/menu/bot").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu/categories/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu/subcategories/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu/subcategories/by-category").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // Для Telegram бота
                        .requestMatchers("/webhook/**").permitAll()

                        // Меню (просмотр, редактирование, добавление)
                        .requestMatchers(HttpMethod.GET, "/api/menu").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/menu/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/menu").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/menu/upload").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/menu/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/menu/**").hasAnyRole("ADMIN", "USER")
                        // Заказы (просмотр, изменение статуса)
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasAnyRole("ADMIN", "USER")

                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/me/password").authenticated()

                        // Управление пользователями
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/active").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        // Скрытие товаров
                        .requestMatchers(HttpMethod.PATCH, "/api/menu/*/visibility").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu/*").hasRole("ADMIN")
                        // Отчёты
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

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