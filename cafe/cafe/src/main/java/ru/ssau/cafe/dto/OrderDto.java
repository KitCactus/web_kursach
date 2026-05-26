package ru.ssau.cafe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    private Long id;
    private Long clientId;
    private Long clientTelegramId;
    private String clientName;
    private String clientPhone;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;

    public OrderDto() {}

    public OrderDto(Long id, Long clientId, Long clientTelegramId, String clientName, String clientPhone,
                    LocalDateTime orderDate, BigDecimal totalAmount, String status, List<OrderItemDto> items) {
        this.id = id;
        this.clientId = clientId;
        this.clientTelegramId = clientTelegramId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getClientTelegramId() { return clientTelegramId; }
    public void setClientTelegramId(Long clientTelegramId) { this.clientTelegramId = clientTelegramId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
}