package ua.com.obox.dbschema.allergen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllergenRepository extends JpaRepository<Allergen, UUID> {
    Optional<Allergen> findByTranslationId(String translationId);

    Optional<Allergen> findByAllergenId(String allergenId);
    Optional<Allergen> findAllergenByName(String allergenName);

    List<Allergen> findAllByReferenceId(String entityId);
}
