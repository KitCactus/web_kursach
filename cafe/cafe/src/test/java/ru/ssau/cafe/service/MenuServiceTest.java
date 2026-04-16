package ru.ssau.cafe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.cafe.dto.MenuItemDto;
import ru.ssau.cafe.entity.MenuItem;
import ru.ssau.cafe.entity.User;
import ru.ssau.cafe.exception.ResourceNotFoundException;
import ru.ssau.cafe.repository.MenuItemRepository;
import ru.ssau.cafe.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для MenuService.
 * Проверяет бизнес-логику: валидацию, создание, получение блюд.
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MenuService menuService;

    private User testUser;
    private MenuItem testItem;

    @BeforeEach
    void setUp() {
        testUser = new User("admin", "password", "admin@test.com", "Администратор", "+79001234567");
        testUser.setId(1L);

        testItem = new MenuItem("Капучино", "Классический капучино", new BigDecimal("250.00"), "COFFEE", null);
        testItem.setId(1L);
        testItem.setIsAvailable(true);
        testItem.setIsHidden(false);
        testItem.setCreatedBy(testUser);
    }

    @Test
    @DisplayName("getAllMenuItems — возвращает список всех блюд")
    void getAllMenuItems_returnsAllItems() {
        when(menuItemRepository.findAll()).thenReturn(List.of(testItem));

        List<MenuItemDto> result = menuService.getAllMenuItems();

        assertEquals(1, result.size());
        assertEquals("Капучино", result.get(0).getName());
    }

    @Test
    @DisplayName("getMenuItemById — возвращает блюдо по ID")
    void getMenuItemById_existingId_returnsItem() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        MenuItemDto result = menuService.getMenuItemById(1L);

        assertEquals("Капучино", result.getName());
        assertEquals(new BigDecimal("250.00"), result.getPrice());
        assertEquals("COFFEE", result.getCategory());
    }

    @Test
    @DisplayName("getMenuItemById — несуществующий ID бросает исключение")
    void getMenuItemById_notFound_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> menuService.getMenuItemById(999L));
    }

    @Test
    @DisplayName("createMenuItem — пустое название бросает исключение")
    void createMenuItem_emptyName_throwsException() {
        MenuItemDto dto = new MenuItemDto(null, "", "Описание", new BigDecimal("100"), "COFFEE", null, null, true, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> menuService.createMenuItem(dto, 1L));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    @DisplayName("createMenuItem — нулевая цена бросает исключение")
    void createMenuItem_zeroPrice_throwsException() {
        MenuItemDto dto = new MenuItemDto(null, "Чай", "Описание", BigDecimal.ZERO, "TEA", null, null, true, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> menuService.createMenuItem(dto, 1L));
        assertEquals("Цена должна быть больше 0", ex.getMessage());
    }

    @Test
    @DisplayName("createMenuItem — корректные данные сохраняют блюдо")
    void createMenuItem_validData_savesAndReturnsItem() {
        MenuItemDto dto = new MenuItemDto(null, "Латте", "Нежный латте", new BigDecimal("280.00"), "COFFEE", "Латте", null, true, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);

        MenuItemDto result = menuService.createMenuItem(dto, 1L);

        assertNotNull(result);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("updateAvailability — меняет флаг доступности")
    void updateAvailability_togglesFlag() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);

        menuService.updateAvailability(1L, false);

        assertFalse(testItem.getIsAvailable());
        verify(menuItemRepository).save(testItem);
    }

    @Test
    @DisplayName("deleteMenuItem — удаляет существующее блюдо")
    void deleteMenuItem_existingItem_deletesIt() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        menuService.deleteMenuItem(1L);

        verify(menuItemRepository, times(1)).delete(testItem);
    }
}
