package ua.com.obox.dbschema.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.category.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}