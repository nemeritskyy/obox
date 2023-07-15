package ua.com.obox.dbschema.confirmation;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.menu.MenuItem;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByCategoryMenuRestaurantTenantName(String tenantName);
}