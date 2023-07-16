package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId) {
        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);
        List<RestaurantResponse> responseList = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            RestaurantResponse response = RestaurantResponse.builder()
                    .restaurantId(restaurant.getRestaurantId())
                    .address(restaurant.getAddress())
                    .name(restaurant.getName())
                    .tenantId(restaurant.getTenant().getTenantId())
                    .build();
            responseList.add(response);
        }

        return responseList;
    }



    public TenantResponse getTenantById(String tenantId) {
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

    public TenantResponseId createTenant(Tenant request) {
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .build();
        tenantRepository.save(tenant);
//        Map<String, String> response = new HashMap<>();
//        response.put("tenant_id", tenant.getTenantId());
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
//        Map<String, String> response = new HashMap<>();
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenant.setName(request.getName()); // update name
            tenantRepository.save(tenant);
//            response.put("tenant_id", tenant.getTenantId());
        }
//        return TenantResponseId.builder()
//                .tenantId(tenant.getTenantId())
//                .build();
    }

    public void deleteTenantById(String tenantId) {
//        Map<String, String> response = new HashMap<>();
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenantRepository.delete(tenant);
//            response.put("tenant_id", tenant.getTenantId());
        }
//        return TenantResponseId.builder()
//                .tenantId(tenant.getTenantId())
//                .build();
    }

}
