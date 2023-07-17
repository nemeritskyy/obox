package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
        if (restaurants.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurants with Tenant id " + tenantId + " not found", null);
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
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenant.setName(request.getName());
            tenantRepository.save(tenant);
        }
    }

    public void deleteTenantById(String tenantId) {
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        if (tenant != null) {
            tenantRepository.delete(tenant);
        }
    }

}