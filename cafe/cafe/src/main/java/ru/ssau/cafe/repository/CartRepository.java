package ru.ssau.cafe.repository;

import ru.ssau.cafe.entity.CartItem;
import ru.ssau.cafe.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByClient(Client client);

    List<CartItem> findByClientId(Long clientId);

    void deleteByClientId(Long clientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.client.id = :clientId")
    void clearCart(@Param("clientId") Long clientId);
}