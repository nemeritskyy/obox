package ua.com.obox.dbschema.dish;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {
    Optional<Dish> findByDishId(String dishId);
    Optional<Dish> findByImage(String attachmentId);
    List<Dish> findAllByCategory_CategoryIdOrderByCreatedAtDesc(String categoryId);
}