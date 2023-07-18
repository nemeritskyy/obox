package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final LoggingService loggingService;
    String loggingMessage;

    public RestaurantResponse getRestaurantById(String restaurantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getRestaurantById", restaurantId);
        var tenantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim(), null);
        });
        loggingService.log(LogLevel.INFO, loggingMessage + Message.GET_BY_ID.getMessage());
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createRestaurant", request.toString());
        request.setTenantIdForRestaurant(request.getTenant_id());
        loggingService.log(LogLevel.INFO, "POST tenant id: " + request.getTenant().getTenantId());
        Tenant tenant = tenantRepository.findByTenantId(request.getTenant().getTenantId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.TENANT_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.TENANT_NOT_FOUND.getMessage().trim(), null);
        });
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName().trim()) // delete whitespaces
                .address(request.getAddress().trim())
                .tenant(tenant)
                .build();
        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.CREATE.getMessage());
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchRestaurantById", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim());
        });
        String oldName = restaurant.getName();
        String oldAddress = restaurant.getAddress();
        restaurant.setName(request.getName().trim()); // delete whitespaces
        restaurant.setAddress(request.getAddress().trim()); // delete whitespaces
        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " OLD address=" + oldAddress + " NEW" + Message.UPDATE.getMessage());
    }

    public void deleteRestaurantById(String restaurantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteRestaurantById", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.RESTAURANT_NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.RESTAURANT_NOT_FOUND.getMessage().trim());
        });
        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.DELETE.getMessage() + restaurant.getName());
    }
}
