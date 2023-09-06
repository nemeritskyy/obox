package ua.com.obox.dbschema.dish;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {
    List<Dish> findAllByCategory_CategoryId(String categoryId);

    Optional<Dish> findByDishId(String dishId);
    Optional<Dish> findByImage(String atta);
    List<Dish> findAllByCategory_CategoryIdAndName(String categoryId, String name);

    @Query("SELECT m.restaurant.restaurantId FROM Menu m " +
            "JOIN Category c ON m.menuId = c.menu.menuId " +
            "WHERE c.categoryId = :categoryId")
    String findRestaurantIdByCategoryId(@Param("categoryId") String categoryId);

    @Query("SELECT m.language_code FROM Menu m " +
            "JOIN Category c ON m.menuId = c.menu.menuId " +
            "WHERE c.categoryId = :categoryId")
    String findLanguageCode(@Param("categoryId") String categoryId);
    @Query("SELECT rad.associatedId FROM RestaurantAssociatedData rad " +
            "WHERE rad.restaurantId = :restaurantId and rad.languageCode = :languageCode")
    String findAssociatedIdByRestaurantId(@Param("restaurantId") String restaurantId, @Param("languageCode") String languageCode);
}