package ru.ssau.cafe.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ReportDto {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private Long ordersCount;
    private BigDecimal averageCheck;
    private Map<String, Integer> popularItems;
    private Map<String, Long> salesByCategory;
    private Map<Integer, Integer> ordersByHour;

    public ReportDto() {}

    public ReportDto(LocalDate date, BigDecimal totalRevenue, Long ordersCount) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.ordersCount = ordersCount;
        this.averageCheck = ordersCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(ordersCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public ReportDto(LocalDate date, BigDecimal totalRevenue, Long ordersCount,
                     Map<String, Integer> popularItems, Map<String, Long> salesByCategory,
                     Map<Integer, Integer> ordersByHour) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.ordersCount = ordersCount;
        this.averageCheck = ordersCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(ordersCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        this.popularItems = popularItems;
        this.salesByCategory = salesByCategory;
        this.ordersByHour = ordersByHour;
    }

    // Геттеры и сеттеры
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getOrdersCount() { return ordersCount; }
    public void setOrdersCount(Long ordersCount) { this.ordersCount = ordersCount; }

    public BigDecimal getAverageCheck() { return averageCheck; }
    public void setAverageCheck(BigDecimal averageCheck) { this.averageCheck = averageCheck; }

    public Map<String, Integer> getPopularItems() { return popularItems; }
    public void setPopularItems(Map<String, Integer> popularItems) { this.popularItems = popularItems; }

    public Map<String, Long> getSalesByCategory() { return salesByCategory; }
    public void setSalesByCategory(Map<String, Long> salesByCategory) { this.salesByCategory = salesByCategory; }

    public Map<Integer, Integer> getOrdersByHour() { return ordersByHour; }
    public void setOrdersByHour(Map<Integer, Integer> ordersByHour) { this.ordersByHour = ordersByHour; }
}