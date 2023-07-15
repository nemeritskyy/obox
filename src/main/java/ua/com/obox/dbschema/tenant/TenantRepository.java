package ua.com.obox.dbschema.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.obox.dbschema.tenant.Tenant;

import java.util.Optional;
import java.util.UUID;


public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByName(String tenantName);
    Optional<Tenant> findByTenantId(String tenantId);
}