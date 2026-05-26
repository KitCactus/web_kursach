package ru.ssau.cafe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.ssau.cafe.entity.*;
import ru.ssau.cafe.repository.*;
import ru.ssau.cafe.dto.OrderDto;
import ru.ssau.cafe.dto.OrderEventDto;
import ru.ssau.cafe.service.OrderService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);

    @Value("${telegram.bot.api-key}")
    private String botApiKey;

    private final ClientRepository clientRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    public BotController(ClientRepository clientRepository,
                         CartRepository cartRepository,
                         MenuItemRepository menuItemRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         OrderService orderService,
                         SimpMessagingTemplate messagingTemplate) {
        this.clientRepository = clientRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.messagingTemplate = messagingTemplate;
    }

    private boolean unauthorized(String key) {
        return key == null || !botApiKey.equals(key);
    }

    // ─── Клиенты ────────────────────────────────────────────────────────────

    @GetMapping("/clients/{telegramId}/exists")
    public ResponseEntity<Map<String, Boolean>> clientExists(
            @PathVariable Long telegramId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        boolean exists = clientRepository.findByTelegramId(telegramId).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/clients")
    public ResponseEntity<Map<String, Object>> registerClient(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Long telegramId = Long.valueOf(body.get("telegramId").toString());
            String name = body.get("name").toString();
            String phone = body.get("phone").toString();

            if (clientRepository.findByTelegramId(telegramId).isPresent()) {
                return ResponseEntity.ok(Map.of("success", false, "reason", "already_exists"));
            }
            Client client = new Client(telegramId, name, phone);
            clientRepository.save(client);
            logger.info("Зарегистрирован новый клиент telegramId={}", telegramId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка регистрации клиента: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "reason", e.getMessage()));
        }
    }

    @GetMapping("/clients/{telegramId}")
    public ResponseEntity<Map<String, Object>> getClientProfile(
            @PathVariable Long telegramId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return clientRepository.findByTelegramId(telegramId).map(c -> {
            Map<String, Object> profile = new HashMap<>();
            profile.put("name", c.getName());
            profile.put("phone", c.getPhone());
            profile.put("registrationDate", c.getRegistrationDate().toString());
            profile.put("totalOrders", c.getTotalOrders());
            profile.put("balance", 0.0);
            return ResponseEntity.ok(profile);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── Корзина ────────────────────────────────────────────────────────────

    @GetMapping("/cart/{telegramId}")
    public ResponseEntity<List<Map<String, Object>>> getCart(
            @PathVariable Long telegramId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<CartItem> items = cartRepository.findByClientTelegramId(telegramId);
        List<Map<String, Object>> result = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("menuItemId", item.getMenuItem() != null ? item.getMenuItem().getId() : null);
            map.put("menuItemName", item.getMenuItem() != null ? item.getMenuItem().getName() : "");
            map.put("quantity", item.getQuantity());
            map.put("price", item.getMenuItem() != null ? item.getMenuItem().getPrice() : BigDecimal.ZERO);
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cart/{telegramId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable Long telegramId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Long menuItemId = Long.valueOf(body.get("menuItemId").toString());
            int quantity = body.containsKey("quantity") ? Integer.parseInt(body.get("quantity").toString()) : 1;

            Client client = clientRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Клиент не найден"));
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            Optional<CartItem> existing = cartRepository.findByClientTelegramIdAndMenuItemId(telegramId, menuItemId);
            if (existing.isPresent()) {
                CartItem item = existing.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartRepository.save(item);
            } else {
                CartItem item = new CartItem(client, menuItem, menuItem.getName(), quantity, menuItem.getPrice());
                cartRepository.save(item);
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка добавления в корзину: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "reason", e.getMessage()));
        }
    }

    @PatchMapping("/cart/items/{cartItemId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            if (quantity <= 0) {
                cartRepository.deleteById(cartItemId);
            } else {
                CartItem item = cartRepository.findById(cartItemId)
                        .orElseThrow(() -> new RuntimeException("Позиция корзины не найдена"));
                item.setQuantity(quantity);
                cartRepository.save(item);
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "reason", e.getMessage()));
        }
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    @Transactional
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        cartRepository.deleteById(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart/{telegramId}")
    @Transactional
    public ResponseEntity<Void> clearCart(
            @PathVariable Long telegramId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        cartRepository.clearCartByTelegramId(telegramId);
        return ResponseEntity.noContent().build();
    }

    // ─── Заказы ─────────────────────────────────────────────────────────────

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Long telegramId = Long.valueOf(body.get("telegramId").toString());

            Client client = clientRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Клиент не найден"));

            List<CartItem> cartItems = cartRepository.findByClientTelegramId(telegramId);
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "reason", "Корзина пуста"));
            }

            BigDecimal total = cartItems.stream()
                    .filter(i -> i.getMenuItem() != null)
                    .map(i -> i.getMenuItem().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Order order = new Order(client, total);
            order = orderRepository.save(order);

            for (CartItem cartItem : cartItems) {
                if (cartItem.getMenuItem() == null) continue;
                MenuItem menuItem = cartItem.getMenuItem();
                OrderItem orderItem = new OrderItem(
                        order,
                        menuItem,
                        cartItem.getQuantity(),
                        menuItem.getPrice(),
                        menuItem.getCategory() != null ? menuItem.getCategory() : "OTHER"
                );
                orderItemRepository.save(orderItem);
            }

            cartRepository.clearCartByTelegramId(telegramId);
            client.setTotalOrders(client.getTotalOrders() + 1);
            clientRepository.save(client);

            OrderDto orderDto = orderService.getOrderById(order.getId());
            messagingTemplate.convertAndSend("/topic/orders", new OrderEventDto("CREATED", orderDto));

            logger.info("Создан заказ #{} для telegramId={} на сумму {}", order.getId(), telegramId, total);
            return ResponseEntity.ok(Map.of("success", true, "orderId", order.getId()));
        } catch (Exception e) {
            logger.error("Ошибка создания заказа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "reason", e.getMessage()));
        }
    }

    // ─── Заказы клиента ─────────────────────────────────────────────────────

    @GetMapping("/orders/{telegramId}")
    public ResponseEntity<List<Map<String, Object>>> getClientOrders(
            @PathVariable Long telegramId,
            @RequestHeader(value = "X-Bot-Api-Key", required = false) String apiKey) {
        if (unauthorized(apiKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<OrderDto> orders = orderService.getAllOrders().stream()
                .filter(o -> telegramId.equals(o.getClientTelegramId()))
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .toList();

        List<Map<String, Object>> result = orders.stream().map(o -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", o.getId());
            map.put("status", o.getStatus());
            map.put("orderDate", o.getOrderDate().toString());
            map.put("totalAmount", o.getTotalAmount());
            map.put("items", o.getItems().stream().map(item -> {
                Map<String, Object> i = new java.util.HashMap<>();
                i.put("name", item.getMenuItemName());
                i.put("quantity", item.getQuantity());
                i.put("price", item.getPrice());
                return i;
            }).toList());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }
}
