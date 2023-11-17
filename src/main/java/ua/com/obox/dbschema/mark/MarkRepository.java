package ua.com.obox.dbschema.mark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarkRepository extends JpaRepository<Mark, UUID> {
    Optional<Mark> findByMarkId(String markId);
    List<Mark> findAllByReferenceId(String entityId);
    List<Mark> findAllByReferenceIdOrderByCreatedAtDesc(String entityId);
}
