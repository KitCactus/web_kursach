package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.ReportDto;
import ru.ssau.cafe.entity.Order;
import ru.ssau.cafe.repository.OrderItemRepository;
import ru.ssau.cafe.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public ReportService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public ReportDto getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByDateRange(start, end);

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long ordersCount = orders.size();

        // Топ блюда за день
        Map<String, Integer> popularItems = new LinkedHashMap<>();
        for (Object[] row : orderItemRepository.findMostPopularItemsByDate(start, end)) {
            popularItems.put((String) row[0], ((Number) row[1]).intValue());
        }

        // Продажи по категориям за день
        Map<String, Long> salesByCategory = new LinkedHashMap<>();
        for (Object[] row : orderItemRepository.getSalesByCategoryByDate(start, end)) {
            if (row[0] != null) salesByCategory.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Заказы по часам за день
        Map<Integer, Integer> ordersByHour = new LinkedHashMap<>();
        for (Object[] row : orderItemRepository.getOrdersByHour(start, end)) {
            ordersByHour.put(((Number) row[0]).intValue(), ((Number) row[1]).intValue());
        }

        return new ReportDto(date, totalRevenue, ordersCount, popularItems, salesByCategory, ordersByHour);
    }

    public List<ReportDto> getWeeklyReport(LocalDate startDate) {
        List<ReportDto> reports = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            reports.add(getDailyReport(startDate.plusDays(i)));
        }
        return reports;
    }

    public Map<String, Integer> getPopularItems() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Object[] row : orderItemRepository.findMostPopularItems()) {
            result.put((String) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }

    public Map<String, Long> getSalesByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : orderItemRepository.getSalesByCategory()) {
            if (row[0] != null) result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }
}
