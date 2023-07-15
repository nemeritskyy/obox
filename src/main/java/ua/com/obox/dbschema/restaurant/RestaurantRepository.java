package ua.com.obox.dbschema.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.tenant.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findByTenant_Name(String tenantName);
}