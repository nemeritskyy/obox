package ua.com.obox.dbschema.language;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<Language, UUID> {
    List<Language> findAll();
    Optional<Language> findByName(String languageName);
    Optional<Language> findByLanguageId(String id);
}