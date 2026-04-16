package ru.ssau.cafe.repository;

import ru.ssau.cafe.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.menuItem.id, SUM(oi.quantity) FROM OrderItem oi " +
            "GROUP BY oi.menuItem.id ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostPopularItems();

    @Query("SELECT oi.category, SUM(oi.quantity) FROM OrderItem oi " +
            "GROUP BY oi.category")
    List<Object[]> getSalesByCategory();
}