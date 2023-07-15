package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    Tenant tenant;

    public TenantResponse getTenantById(String tenantId) {
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

    public TenantResponse getTenantInfo(String tenantName) {
        var tenantInfo = tenantRepository.findByName(tenantName);
        Tenant tenant;
        if (tenantInfo.isPresent()) {
            tenant = tenantInfo.get();
        } else {
            throw new NoSuchElementException("No found tenant");
        }
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

}
