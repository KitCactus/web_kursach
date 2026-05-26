package ru.ssau.cafe.repository;

import ru.ssau.cafe.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategory(String category);

    List<MenuItem> findByIsAvailableTrue();

    List<MenuItem> findByIsHiddenFalse();

    @Query("SELECT m FROM MenuItem m WHERE m.isHidden = false AND m.isAvailable = true")
    List<MenuItem> findAvailableForBot();

    @Query("SELECT m FROM MenuItem m WHERE m.isHidden = false AND m.isAvailable = true AND m.subcategory = :subcategory")
    List<MenuItem> findAvailableBySubcategory(@Param("subcategory") String subcategory);

    @Query("SELECT m FROM MenuItem m WHERE m.isHidden = false AND m.isAvailable = true AND m.name = :name")
    List<MenuItem> findAvailableByExactName(@Param("name") String name);

    @Query("SELECT m FROM MenuItem m WHERE m.isHidden = false AND m.isAvailable = true AND m.name = :name AND m.subcategory = :subcategory")
    List<MenuItem> findAvailableByNameAndSubcategory(@Param("name") String name, @Param("subcategory") String subcategory);

    @Query("SELECT m FROM MenuItem m WHERE m.isHidden = false AND m.isAvailable = true AND m.name = :name AND m.description = :description")
    Optional<MenuItem> findAvailableByNameAndDescription(@Param("name") String name, @Param("description") String description);

    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MenuItem> searchByName(@Param("name") String name);

    @Query("SELECT m FROM MenuItem m WHERE m.category = :category AND m.isHidden = false")
    List<MenuItem> findVisibleByCategory(@Param("category") String category);
}