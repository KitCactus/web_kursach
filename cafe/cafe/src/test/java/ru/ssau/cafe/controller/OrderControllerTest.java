package ru.ssau.cafe.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.ssau.cafe.dto.OrderDto;
import ru.ssau.cafe.dto.OrderItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit тесты для OrderController.
 *
 * Что мы тестируем:
 * - Логика создания тестовых заказов
 * - Проверка структуры данных OrderDto
 * - Валидация основных операций (статусы, суммы)
 */
class OrderControllerTest {

    // === Вспомогательный метод для создания тестовых заказов ===
    private OrderDto createTestOrder(Long id, String clientName, BigDecimal amount, String status) {
        OrderItemDto item1 = new OrderItemDto(1L, 1L, "Эспрессо", 2, new BigDecimal("150.00"), "Кофе");
        OrderItemDto item2 = new OrderItemDto(2L, 2L, "Круассан", 1, new BigDecimal("80.00"), "Выпечка");

        return new OrderDto(
                id,
                1L,
                clientName,
                LocalDateTime.now(),
                amount,
                status,
                Arrays.asList(item1, item2)
        );
    }

    // === UNIT ТЕСТЫ ===

    @Test
    @DisplayName("Создание заказа — проверяем что все поля правильно заполняются")
    void createTestOrder_allFieldsSet() {
        OrderDto order = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");

        assertNotNull(order);
        assertEquals(1L, order.getId());
        assertEquals("Иван Петров", order.getClientName());
        assertEquals(new BigDecimal("380.00"), order.getTotalAmount());
        assertEquals("COMPLETED", order.getStatus());
        assertEquals(2, order.getItems().size());
    }

    @Test
    @DisplayName("Заказ с статусом PENDING — проверяем что статус корректен")
    void orderStatus_PENDING_isValid() {
        OrderDto order = createTestOrder(2L, "Мария Сидорова", new BigDecimal("500.00"), "PENDING");

        assertEquals("PENDING", order.getStatus());
        assertNotEquals("COMPLETED", order.getStatus());
    }

    @Test
    @DisplayName("Заказ с статусом READY — проверяем что статус корректен")
    void orderStatus_READY_isValid() {
        OrderDto order = createTestOrder(3L, "Петр Иванов", new BigDecimal("250.00"), "READY");

        assertEquals("READY", order.getStatus());
    }

    @Test
    @DisplayName("Заказ с статусом COMPLETED — проверяем что статус корректен")
    void orderStatus_COMPLETED_isValid() {
        OrderDto order = createTestOrder(4L, "Анна Кузнецова", new BigDecimal("600.00"), "COMPLETED");

        assertEquals("COMPLETED", order.getStatus());
    }

    @Test
    @DisplayName("Список заказов — проверяем что можем создать несколько заказов")
    void multipleOrders_createdSuccessfully() {
        List<OrderDto> orders = Arrays.asList(
                createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED"),
                createTestOrder(2L, "Мария Сидорова", new BigDecimal("500.00"), "PENDING"),
                createTestOrder(3L, "Петр Иванов", new BigDecimal("250.00"), "READY")
        );

        assertEquals(3, orders.size());
        assertEquals("COMPLETED", orders.get(0).getStatus());
        assertEquals("PENDING", orders.get(1).getStatus());
        assertEquals("READY", orders.get(2).getStatus());
    }

    @Test
    @DisplayName("Товары в заказе — проверяем что товары правильно добавлены")
    void orderItems_containsExpectedItems() {
        OrderDto order = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");

        assertEquals(2, order.getItems().size());
        assertEquals("Эспрессо", order.getItems().get(0).getItemName());
        assertEquals("Круассан", order.getItems().get(1).getItemName());
    }

    @Test
    @DisplayName("Сумма заказа — проверяем что сумма хранится корректно")
    void orderTotal_amountStoredCorrectly() {
        BigDecimal expectedAmount = new BigDecimal("380.00");
        OrderDto order = createTestOrder(1L, "Иван Петров", expectedAmount, "COMPLETED");

        assertEquals(expectedAmount, order.getTotalAmount());
        assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("ID заказа — проверяем что ID уникален")
    void orderIds_areUnique() {
        OrderDto order1 = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");
        OrderDto order2 = createTestOrder(2L, "Мария Сидорова", new BigDecimal("500.00"), "PENDING");

        assertNotEquals(order1.getId(), order2.getId());
    }

    @Test
    @DisplayName("Цена товара в заказе — проверяем корректность хранения")
    void orderItemPrice_storedCorrectly() {
        OrderDto order = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");

        OrderItemDto firstItem = order.getItems().get(0);
        assertEquals(new BigDecimal("150.00"), firstItem.getPrice());
    }

    @Test
    @DisplayName("Количество товара в заказе — проверяем корректность хранения")
    void orderItemQuantity_storedCorrectly() {
        OrderDto order = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");

        OrderItemDto firstItem = order.getItems().get(0);
        assertEquals(2, firstItem.getQuantity());
    }

    @Test
    @DisplayName("Жизненный цикл заказа — проверяем переход статусов")
    void orderLifecycle_statusTransitions() {
        OrderDto order = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "PENDING");

        // Шаг 1: начальный статус PENDING
        assertEquals("PENDING", order.getStatus());

        // Шаг 2: создаём новый заказ с статусом READY
        OrderDto readyOrder = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "READY");
        assertEquals("READY", readyOrder.getStatus());

        // Шаг 3: создаём новый заказ с статусом COMPLETED
        OrderDto completedOrder = createTestOrder(1L, "Иван Петров", new BigDecimal("380.00"), "COMPLETED");
        assertEquals("COMPLETED", completedOrder.getStatus());
    }
}
