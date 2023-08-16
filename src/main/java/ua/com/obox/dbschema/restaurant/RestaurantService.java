package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.menu.MenuResponse;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MenuRepository menuRepository;
    private final LoggingService loggingService;
    private final RestaurantServiceHelper serviceHelper;
    private String loggingMessage;

    public List<MenuResponse> getAllMenusByRestaurantId(String restaurantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getAllMenusByRestaurantId", restaurantId);
        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantId(restaurantId);
        if (menus.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menus with Restaurant id " + restaurantId + Message.NOT_FOUND.getMessage().trim(), null);
        }
        List<MenuResponse> responseList = new ArrayList<>();

        for (Menu menu : menus) {
            MenuResponse response = MenuResponse.builder()
                    .menuId(menu.getMenuId())
                    .name(menu.getName())
                    .restaurantId(menu.getRestaurant().getRestaurantId())
                    .build();
            responseList.add(response);
        }

        loggingService.log(LogLevel.INFO, loggingMessage + Message.FIND_COUNT.getMessage() + responseList.size());
        return responseList;
    }

    public RestaurantResponse getRestaurantById(String restaurantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getRestaurantById", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant with id " + restaurantId + Message.NOT_FOUND.getMessage());
        });
        loggingService.log(LogLevel.INFO, loggingMessage);
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createRestaurant", request.getTenant_id());
        request.setTenantIdForRestaurant(request.getTenant_id());
        Tenant tenant = tenantRepository.findByTenantId(request.getTenant().getTenantId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.TENANT_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant with id " + request.getTenant().getTenantId() + Message.NOT_FOUND.getMessage(), null);
        });
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName().trim()) // delete whitespaces
                .tenant(tenant)
                .build();
        if (request.getAddress() != null) {
            serviceHelper.updateStringField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService);
        }
        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + restaurant.getRestaurantId() + Message.CREATE.getMessage());
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchRestaurantById", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant with id " + restaurantId + Message.NOT_FOUND.getMessage());
        });
        String oldName = restaurant.getName();
        String oldAddress = restaurant.getAddress();
        if (request.getName() != null) {
            Validator.validateName(loggingMessage, request.getName(), loggingService);
            restaurant.setName(request.getName().trim()); // delete whitespaces
        }
        if (request.getAddress() != null) {
            serviceHelper.updateStringField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService);
        }
        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW name=" + request.getName() + " OLD address=" + oldAddress + " NEW address=" + request.getAddress() + Message.UPDATE.getMessage());
    }

    public void deleteRestaurantById(String restaurantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteRestaurantById", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant with id " + restaurantId + Message.NOT_FOUND.getMessage());
        });
        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + restaurant.getName() + Message.DELETE.getMessage());
    }
}
