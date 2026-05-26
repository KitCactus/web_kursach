package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.OrderDto;
import ru.ssau.cafe.dto.OrderEventDto;
import ru.ssau.cafe.dto.OrderItemDto;
import ru.ssau.cafe.entity.Order;
import ru.ssau.cafe.entity.OrderItem;
import ru.ssau.cafe.exception.ResourceNotFoundException;
import ru.ssau.cafe.repository.OrderItemRepository;
import ru.ssau.cafe.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.storage.bot-token}")
    private String botToken;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private OrderItemDto convertItemToDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                item.getMenuItem() != null ? item.getMenuItem().getName() : "Deleted item",
                item.getQuantity(),
                item.getPrice(),
                item.getCategory()
        );
    }

    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());

        return new OrderDto(
                order.getId(),
                order.getClient() != null ? order.getClient().getId() : null,
                order.getClient() != null ? order.getClient().getTelegramId() : null,
                order.getClient() != null ? order.getClient().getName() : "Unknown",
                order.getClient() != null ? order.getClient().getPhone() : null,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                itemDtos
        );
    }

    private List<OrderDto> convertToDtoList(List<Order> orders) {
        return orders.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        return convertToDtoList(orderRepository.findAll());
    }

    public List<OrderDto> getOrdersByStatus(String status) {
        return convertToDtoList(orderRepository.findByStatus(status));
    }

    public List<OrderDto> getOrdersByClient(Long clientId) {
        return convertToDtoList(orderRepository.findByClientId(clientId));
    }

    public List<OrderDto> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return convertToDtoList(orderRepository.findByDateRange(start, end));
    }

    public List<OrderDto> getTodayOrders() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return convertToDtoList(orderRepository.findByDateRange(start, end));
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDto(order);
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        BigDecimal revenue = orderRepository.getRevenueForPeriod(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public long getTodayOrdersCount() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return orderRepository.getOrdersCountForPeriod(start, end);
    }

    public BigDecimal getRevenueForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        BigDecimal revenue = orderRepository.getRevenueForPeriod(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Уведомляем клиента в Telegram при готовности или отмене
        if (updatedOrder.getClient() != null && updatedOrder.getClient().getTelegramId() != null) {
            sendStatusNotification(updatedOrder.getClient().getTelegramId(), id, status);
        }

        OrderDto dto = convertToDto(updatedOrder);
        messagingTemplate.convertAndSend("/topic/orders", new OrderEventDto("STATUS_CHANGED", dto));
        return dto;
    }

    private void sendStatusNotification(Long telegramId, Long orderId, String status) {
        String text = switch (status) {
            case "IN_PROGRESS" -> "👨‍🍳 Заказ #" + orderId + " принят в работу!";
            case "PAID" -> "✅ Заказ #" + orderId + " готов! Можете забирать заказ!";
            case "COMPLETED" -> "🎉 Заказ #" + orderId + " завершён. Спасибо за покупку!";
            case "CANCELLED" -> "❌ Заказ #" + orderId + " отменён. Обратитесь к персоналу.";
            default -> null;
        };
        if (text == null) return;

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            restTemplate.postForObject(url, Map.of("chat_id", telegramId, "text", text), Map.class);
            logger.info("Уведомление о статусе {} отправлено telegramId={}", status, telegramId);
        } catch (Exception e) {
            logger.warn("Не удалось отправить уведомление telegramId={}: {}", telegramId, e.getMessage());
        }
    }
}
