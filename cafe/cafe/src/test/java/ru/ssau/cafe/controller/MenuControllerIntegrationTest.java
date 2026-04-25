package ru.ssau.cafe.controller;

import ru.ssau.cafe.entity.MenuItem;
import ru.ssau.cafe.entity.Role;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.repository.MenuItemRepository;
import ru.ssau.cafe.repository.RoleRepository;
import ru.ssau.cafe.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Base64;

@SpringBootTest
@AutoConfigureMockMvc
public class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String basicAuthHeader;
    private User testUser;

    @BeforeEach
    void setUp() {
        menuItemRepository.deleteAllInBatch();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        testUser = new User("testadmin", passwordEncoder.encode("pass123"), null, "Test Admin", "79001234567");
        testUser.setRoles(List.of(role));
        userRepository.save(testUser);

        basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString("testadmin:pass123".getBytes());

        MenuItem item1 = new MenuItem("Латте", "Кофе с молоком", BigDecimal.valueOf(250), "COFFEE", "latte");
        item1.setCreatedBy(testUser);
        MenuItem item2 = new MenuItem("Капучино", "Кофе с пенкой", BigDecimal.valueOf(200), "COFFEE", "cappuccino");
        item2.setCreatedBy(testUser);
        menuItemRepository.save(item1);
        menuItemRepository.save(item2);
    }

    @AfterEach
    void tearDown() {
        menuItemRepository.deleteAllInBatch();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void getMenu_authenticated_returns200WithItems() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .header("Authorization", basicAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Латте")));
    }

    @Test
    void getMenu_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMenuItem_validData_returns201() throws Exception {
        String json = """
                {
                  "name": "Эспрессо",
                  "description": "Крепкий кофе",
                  "price": 150,
                  "category": "COFFEE",
                  "subcategory": "espresso",
                  "isAvailable": true,
                  "isHidden": false
                }
                """;

        mockMvc.perform(post("/api/menu")
                        .header("Authorization", basicAuthHeader)
                        .param("userId", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Эспрессо")))
                .andExpect(jsonPath("$.price", is(150)));
    }

    @Test
    void getMenuItemById_existingId_returnsItem() throws Exception {
        Long id = menuItemRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/menu/{id}", id)
                        .header("Authorization", basicAuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())));
    }

    @Test
    void getMenuItemById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/menu/{id}", 99999L)
                        .header("Authorization", basicAuthHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMenuItem_existingId_returns204() throws Exception {
        Long id = menuItemRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/menu/{id}", id)
                        .header("Authorization", basicAuthHeader))
                .andExpect(status().isNoContent());
    }
}
