package ua.com.obox.dbschema.sorting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EntityOrderRepository extends JpaRepository<EntityOrder, UUID> {
    Optional<EntityOrder> findByEntityId(String entityId);

    Optional<EntityOrder> findBySortedListContaining(String entityId);
}