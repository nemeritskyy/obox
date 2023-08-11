package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.menu.MenuResponse;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService extends AbstractResponseService {
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MenuRepository menuRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<MenuResponse> getAllMenusByRestaurantId(String restaurantId) {
        loggingMessage = "getAllMenusByRestaurantId";
        responseMessage = String.format("Menus with Restaurant id %s", restaurantId);

        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantId(restaurantId);
        if (menus.isEmpty()) {
            notFoundResponse(restaurantId);
        }

        List<MenuResponse> responseList = menus.stream()
                .map(menu -> MenuResponse.builder()
                        .menuId(menu.getMenuId())
                        .name(menu.getName())
                        .restaurantId(menu.getRestaurant().getRestaurantId())
                        .language(menu.getLanguage_code())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("%s %s %s %d", loggingMessage, restaurantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public RestaurantResponse getRestaurantById(String restaurantId) {
        Restaurant restaurant;
        loggingMessage = "getRestaurantById";
        responseMessage = String.format("Restaurant with id %s", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurant = restaurantInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, restaurantId));
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request) {
        Tenant tenant;
        Restaurant restaurant;
        loggingMessage = "createRestaurant";
        responseMessage = String.format("Tenant with id %s", request.getTenant_id());

        request.setTenantIdForRestaurant(request.getTenant_id());

        tenant = tenantRepository.findByTenantId(request.getTenant().getTenantId()).orElseThrow(() -> {
            badRequestResponse(request.getTenant().getTenantId());
            return null;
        });

        restaurant = Restaurant.builder()
                .name(request.getName().trim())
                .tenant(tenant)
                .build();

        serviceHelper.updateVarcharField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService);

        restaurantRepository.save(restaurant);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), restaurant.getRestaurantId(), Message.CREATE.getMessage()));
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request) {
        Restaurant restaurant;
        loggingMessage = "patchRestaurantById";
        responseMessage = String.format("Restaurant with id %s", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurant = restaurantInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        serviceHelper.updateNameField(restaurant::setName, request.getName(), "Name", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService);

        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, restaurantId, Message.UPDATE.getMessage()));
    }

    public void deleteRestaurantById(String restaurantId) {
        Restaurant restaurant;
        loggingMessage = "restaurantId";
        responseMessage = String.format("Restaurant with id %s", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurant = restaurantInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, restaurantId, restaurant.getName(), Message.DELETE.getMessage()));
    }

    @Override
    public void notFoundResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }

    @Override
    public void badRequestResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.BAD_REQUEST,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }
}
