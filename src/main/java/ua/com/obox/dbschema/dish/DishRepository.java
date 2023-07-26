package ua.com.obox.dbschema.dish;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {
    List<Dish> findAllByCategory_CategoryId(String categoryId);

    Optional<Dish> findByDishId(String dishId);
}