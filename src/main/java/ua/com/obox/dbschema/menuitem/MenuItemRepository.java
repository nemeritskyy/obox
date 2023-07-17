package ua.com.obox.dbschema.menuitem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findAllByCategoryMenuRestaurantTenantName(String tenantName);
}