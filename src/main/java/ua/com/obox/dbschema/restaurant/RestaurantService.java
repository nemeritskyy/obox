package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tenant.TenantRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantResponse getRestaurantInfo(String tenantName) {
        System.out.println(tenantName.toString());
        Restaurant restaurant = restaurantRepository.findByTenant_Name(tenantName).get();
        System.out.println(restaurant.getTenant().getTenantId());
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }
}
