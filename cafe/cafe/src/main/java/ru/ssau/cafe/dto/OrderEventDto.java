package ru.ssau.cafe.dto;

public class OrderEventDto {
    private String type; // "CREATED", "STATUS_CHANGED"
    private OrderDto order;

    public OrderEventDto(String type, OrderDto order) {
        this.type = type;
        this.order = order;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public OrderDto getOrder() { return order; }
    public void setOrder(OrderDto order) { this.order = order; }
}
