package ua.com.obox.dbschema.allergen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllergenRepository extends JpaRepository<Allergen, UUID> {
    Optional<Allergen> findByAllergenId(String allergenId);
    List<Allergen> findAllByReferenceId(String entityId);
    List<Allergen> findAllByReferenceIdOrderByCreatedAtDesc(String entityId);
}
