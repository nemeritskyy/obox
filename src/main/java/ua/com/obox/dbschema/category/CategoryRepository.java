package ua.com.obox.dbschema.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByCategoryId(String categoryId);
    List<Category> findAllByMenu_MenuId(String menuId);
}