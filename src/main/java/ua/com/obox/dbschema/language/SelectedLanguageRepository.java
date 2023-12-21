package ua.com.obox.dbschema.language;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SelectedLanguageRepository extends JpaRepository<SelectedLanguage, UUID> {
    List<SelectedLanguage> findByRestaurantId(String restaurantId);

    Optional<SelectedLanguage> findByLanguageIdAndRestaurantId(String languageId, String restaurantId);
}