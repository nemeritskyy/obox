package ua.com.obox.dbschema.sorting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityOrderRepository extends JpaRepository<EntityOrder, UUID> {
    List<EntityOrder> findByReferenceId(String entityId);
    Optional<EntityOrder> findByReferenceIdAndReferenceType(String entityId, String referenceType);

    Optional<EntityOrder> findBySortedListContaining(String entityId);
}