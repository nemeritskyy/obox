package ua.com.obox.dbschema.language;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SelectedLanguagesRepository extends JpaRepository<SelectedLanguages, UUID> {
    Optional<SelectedLanguages> findByRestaurantId(String restaurantId);
}