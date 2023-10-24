package ua.com.obox.dbschema.translation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TranslationRepository extends JpaRepository<Translation, UUID> {
    Optional<Translation> findAllByTranslationId(String translationId);
    Optional<Translation> findAllByReferenceId(String referenceId);
}
