package ru.ssau.cafe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Проверяет что Spring-контекст запускается без ошибок.
 * Использует H2 вместо PostgreSQL (application-test.properties).
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CafeApplicationTests {

    @Test
    void contextLoads() {
        // Если тест прошёл — контекст успешно поднялся
    }
}
