package ua.com.obox.dbschema.confirmation;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.menu.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}