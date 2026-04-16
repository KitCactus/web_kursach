package ru.ssau.cafe.service;

import ru.ssau.cafe.dto.ReportDto;
import ru.ssau.cafe.repository.OrderItemRepository;
import ru.ssau.cafe.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        BigDecimal totalRevenue = orderRepository.findByDateRange(start, end).stream()
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long ordersCount = orderRepository.findByDateRange(start, end).size();

        return new ReportDto(date, totalRevenue, ordersCount);
    }

    public List<ReportDto> getWeeklyReport(LocalDate startDate) {
        List<ReportDto> reports = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            reports.add(getDailyReport(date));
        }
        return reports;
    }

    public Map<String, Integer> getPopularItems() {
        List<Object[]> results = orderItemRepository.findMostPopularItems();
        Map<String, Integer> popularItems = new HashMap<>();
        for (Object[] result : results) {
            Long itemId = (Long) result[0];
            Long quantity = (Long) result[1];
            popularItems.put("Item " + itemId, quantity.intValue());
        }
        return popularItems;
    }

    public Map<String, Long> getSalesByCategory() {
        List<Object[]> results = orderItemRepository.getSalesByCategory();
        Map<String, Long> salesByCategory = new HashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Long quantity = (Long) result[1];
            salesByCategory.put(category, quantity);
        }
        return salesByCategory;
    }
}