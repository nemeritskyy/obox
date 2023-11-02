package ua.com.obox.dbschema.associateddata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantAssociatedDataRepository extends JpaRepository<RestaurantAssociatedData, UUID> {
    Optional<RestaurantAssociatedData> findByRestaurantIdAndLanguageCode(String restaurantId, String languageCode);

    Optional<RestaurantAssociatedData> findByAssociatedId(String associatedId);

    Optional<RestaurantAssociatedData> findByRestaurantId(String restaurantId);
}
