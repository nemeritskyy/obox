package ua.com.obox.dbschema.confirmation;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.menu.Menu;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}