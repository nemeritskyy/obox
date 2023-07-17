package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tenant.TenantResponse;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantResponse getRestaurantById(String restaurantId) {
        var tenantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = tenantInfo.orElseThrow(() -> new NoSuchElementException("No found tenant"));
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

//    public RestaurantResponse getRestaurantInfo(String tenantName) {
//        System.out.println(tenantName.toString());
//        Restaurant restaurant = restaurantRepository.findByTenant_Name(tenantName).get();
//        System.out.println(restaurant.getTenant().getTenantId());
//        return RestaurantResponse.builder()
//                .restaurantId(restaurant.getRestaurantId())
//                .address(restaurant.getAddress())
//                .name(restaurant.getName())
//                .tenantId(restaurant.getTenant().getTenantId())
//                .build();
//    }
}
