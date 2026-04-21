package ru.ssau.cafe.controller;

import ru.ssau.cafe.dto.OrderDto;
import ru.ssau.cafe.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private static final List<String> VALID_STATUSES = Arrays.asList(
            "PENDING", "IN_PROGRESS", "PAID", "COMPLETED", "CANCELLED"
    );

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/today")
    public ResponseEntity<List<OrderDto>> getTodayOrders() {
        return ResponseEntity.ok(orderService.getTodayOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/today/revenue")
    public ResponseEntity<BigDecimal> getTodayRevenue() {
        return ResponseEntity.ok(orderService.getTodayRevenue());
    }

    @GetMapping("/today/count")
    public ResponseEntity<Long> getTodayOrdersCount() {
        return ResponseEntity.ok(orderService.getTodayOrdersCount());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status + ". Valid statuses: " + VALID_STATUSES);
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}