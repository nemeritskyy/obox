package ua.com.obox.dbschema.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findAllByTenant_TenantId(String tenantId);

    Optional<Restaurant> findByRestaurantId(String restaurantId);
}