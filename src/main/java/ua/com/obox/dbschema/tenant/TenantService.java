package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantResponse getTenantById(String tenantId) {
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

    public Map<String, String> createTenant(Tenant request) {
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .build();
        tenantRepository.save(tenant);

        Map<String, String> response = new HashMap<>();
        response.put("tenant_id", tenant.getTenantId());
        return response;
    }

    public Map<String, String> patchTenantById(String tenantId, Tenant request) {
        Map<String, String> response = new HashMap<>();
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenant.setName(request.getName()); // update name
            tenantRepository.save(tenant);
            response.put("tenant_id", tenant.getTenantId());
        }
        return response;
    }

    public Map<String, String> deleteTenantById(String tenantId) {
        Map<String, String> response = new HashMap<>();
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenantRepository.delete(tenant);
            response.put("tenant_id", tenant.getTenantId());
        }
        return response;
    }

}
