package ru.ssau.cafe.repository;

import ru.ssau.cafe.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.menuItem.name, SUM(oi.quantity), SUM(oi.price * oi.quantity) FROM OrderItem oi " +
            "WHERE oi.menuItem IS NOT NULL " +
            "GROUP BY oi.menuItem.id, oi.menuItem.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostPopularItems();

    @Query("SELECT oi.menuItem.name, SUM(oi.quantity), SUM(oi.price * oi.quantity) FROM OrderItem oi " +
            "WHERE oi.menuItem IS NOT NULL AND oi.order.orderDate BETWEEN :start AND :end " +
            "GROUP BY oi.menuItem.id, oi.menuItem.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostPopularItemsByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.menuItem.category, SUM(oi.quantity), SUM(oi.price * oi.quantity) FROM OrderItem oi " +
            "GROUP BY oi.menuItem.category")
    List<Object[]> getSalesByCategory();

    @Query("SELECT oi.menuItem.category, SUM(oi.quantity), SUM(oi.price * oi.quantity) FROM OrderItem oi " +
            "WHERE oi.order.orderDate BETWEEN :start AND :end " +
            "GROUP BY oi.menuItem.category")
    List<Object[]> getSalesByCategoryByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT EXTRACT(HOUR FROM order_date) AS hour, COUNT(*) AS cnt " +
            "FROM orders WHERE order_date BETWEEN :start AND :end " +
            "GROUP BY EXTRACT(HOUR FROM order_date) ORDER BY hour",
            nativeQuery = true)
    List<Object[]> getOrdersByHour(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}