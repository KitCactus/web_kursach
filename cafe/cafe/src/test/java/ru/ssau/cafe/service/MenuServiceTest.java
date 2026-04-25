package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.MenuItemDto;
import ru.ssau.cafe.entity.MenuItem;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.exception.ResourceNotFoundException;
import ru.ssau.cafe.repository.MenuItemRepository;
import ru.ssau.cafe.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MenuServiceTest {

    private final MenuItemRepository menuItemRepository = mock(MenuItemRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MenuService menuService = new MenuService(menuItemRepository, userRepository);

    private MenuItem makeItem(Long id, String name, String category, BigDecimal price) {
        MenuItem item = new MenuItem(name, "desc", price, category, "sub");
        item.setId(id);
        return item;
    }

    @Test
    void getAllMenuItems_returnsDtoList() {
        when(menuItemRepository.findAll()).thenReturn(List.of(
                makeItem(1L, "Латте", "COFFEE", BigDecimal.valueOf(250)),
                makeItem(2L, "Капучино", "COFFEE", BigDecimal.valueOf(200))
        ));

        List<MenuItemDto> result = menuService.getAllMenuItems();

        assertEquals(2, result.size());
        assertEquals("Латте", result.get(0).getName());
        assertEquals("Капучино", result.get(1).getName());
    }

    @Test
    void getMenuItemById_existingId_returnsDto() {
        MenuItem item = makeItem(1L, "Латте", "COFFEE", BigDecimal.valueOf(250));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        MenuItemDto result = menuService.getMenuItemById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Латте", result.getName());
    }

    @Test
    void getMenuItemById_notFound_throwsException() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> menuService.getMenuItemById(99L));
    }

    @Test
    void createMenuItem_validData_savesAndReturnsDto() {
        User user = new User("admin", "pass", null, "Admin", "79001234567");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MenuItemDto dto = new MenuItemDto(null, "Эспрессо", "desc", BigDecimal.valueOf(150), "COFFEE", "espresso", null, true, false);
        MenuItem saved = makeItem(10L, "Эспрессо", "COFFEE", BigDecimal.valueOf(150));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(saved);

        MenuItemDto result = menuService.createMenuItem(dto, 1L);

        assertEquals("Эспрессо", result.getName());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void createMenuItem_emptyName_throwsException() {
        User user = new User("admin", "pass", null, "Admin", "79001234567");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MenuItemDto dto = new MenuItemDto(null, "", "desc", BigDecimal.valueOf(150), "COFFEE", "sub", null, true, false);

        assertThrows(RuntimeException.class, () -> menuService.createMenuItem(dto, 1L));
    }

    @Test
    void createMenuItem_zeroPrice_throwsException() {
        User user = new User("admin", "pass", null, "Admin", "79001234567");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MenuItemDto dto = new MenuItemDto(null, "Чай", "desc", BigDecimal.ZERO, "TEA", "sub", null, true, false);

        assertThrows(RuntimeException.class, () -> menuService.createMenuItem(dto, 1L));
    }

    @Test
    void updateAvailability_existingItem_updatesFlag() {
        MenuItem item = makeItem(1L, "Латте", "COFFEE", BigDecimal.valueOf(250));
        item.setIsAvailable(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(menuItemRepository.save(any())).thenReturn(item);

        MenuItemDto result = menuService.updateAvailability(1L, false);

        assertFalse(result.getIsAvailable());
    }

    @Test
    void deleteMenuItem_existingItem_deletesIt() {
        MenuItem item = makeItem(1L, "Латте", "COFFEE", BigDecimal.valueOf(250));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        menuService.deleteMenuItem(1L);

        verify(menuItemRepository).delete(item);
    }

    @Test
    void getAllCategories_returnsDistinctSorted() {
        when(menuItemRepository.findAll()).thenReturn(List.of(
                makeItem(1L, "Латте", "COFFEE", BigDecimal.valueOf(250)),
                makeItem(2L, "Капучино", "COFFEE", BigDecimal.valueOf(200)),
                makeItem(3L, "Чай", "TEA", BigDecimal.valueOf(100))
        ));

        List<String> result = menuService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("COFFEE", result.get(0));
        assertEquals("TEA", result.get(1));
    }
}
