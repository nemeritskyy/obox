package ua.com.obox.dbschema.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findAllByRestaurant_RestaurantIdOrderByName(String restaurantId);

    Optional<Menu> findByMenuId(String menuId);
    List<Menu> findAllByRestaurant_RestaurantIdAndName(String restaurantId, String name);
}